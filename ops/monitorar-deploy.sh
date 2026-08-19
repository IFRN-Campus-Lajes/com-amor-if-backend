#!/usr/bin/env bash

# Executa uma única verificação. Programe-o com systemd ou cron para rodar
# periodicamente na VM.
set -Eeuo pipefail

CONFIG_FILE="${DEPLOY_MONITOR_CONFIG:-/deploy/.env.monitor}"

if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Arquivo de configuração não encontrado: $CONFIG_FILE" >&2
    exit 1
fi

# shellcheck disable=SC1090
source "$CONFIG_FILE"

: "${DEPLOY_DIR:?DEPLOY_DIR deve ser definido}"
: "${BACKEND_DIR:?BACKEND_DIR deve ser definido}"
: "${FRONTEND_DIR:?FRONTEND_DIR deve ser definido}"
: "${COMPOSE_FILE:?COMPOSE_FILE deve ser definido}"
: "${LOG_FILE:?LOG_FILE deve ser definido}"
: "${EMAIL_TO:?EMAIL_TO deve ser definido}"
: "${SMTP_URL:?SMTP_URL deve ser definido}"
: "${SMTP_USERNAME:?SMTP_USERNAME deve ser definido}"
: "${SMTP_PASSWORD:?SMTP_PASSWORD deve ser definido}"
: "${SMTP_FROM:?SMTP_FROM deve ser definido}"

mkdir -p "$(dirname "$LOG_FILE")"

log() {
    local message="$1"
    printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S %z')" "$message" | tee -a "$LOG_FILE"
}

send_email() {
    local subject="$1"
    local message="$2"

    if ! printf 'From: %s\r\nTo: %s\r\nSubject: %s\r\n\r\n%s\r\n' \
        "$SMTP_FROM" "$EMAIL_TO" "$subject" "$message" | curl --silent --show-error --fail \
        --url "$SMTP_URL" \
        --ssl-reqd \
        --user "$SMTP_USERNAME:$SMTP_PASSWORD" \
        --mail-from "$SMTP_FROM" \
        --mail-rcpt "$EMAIL_TO" \
        --upload-file -; then
        log "Não foi possível enviar o e-mail: $subject"
        return 1
    fi

    log "E-mail enviado: $subject"
}

on_error() {
    local line="$1"
    local exit_code="$2"
    local message

    trap - ERR
    message="O monitor encontrou um erro na linha $line (código $exit_code).

Consulte o log na VM:
$LOG_FILE"
    log "$message"
    send_email "Falha no monitor de deploy - Com Amor IF" "$message" || true
    exit "$exit_code"
}

trap 'on_error "$LINENO" "$?"' ERR

deployment_error() {
    local message="$1"

    log "$message"
    send_email "Falha no deploy - Com Amor IF" "$message" || true
    exit 1
}

sync_repository() {
    local name="$1"
    local directory="$2"
    local local_commit
    local remote_commit
    local current_branch

    if [[ ! -d "$directory/.git" ]]; then
        log "$name: repositório não encontrado em $directory"
        return 2
    fi

    current_branch=$(git -C "$directory" branch --show-current) || return 2
    if [[ "$current_branch" != "deploy" ]]; then
        log "$name: a branch atual é '$current_branch'; esperado: 'deploy'."
        return 2
    fi

    if [[ -n "$(git -C "$directory" status --porcelain)" ]]; then
        log "$name: há alterações locais; atualização cancelada para protegê-las."
        return 2
    fi

    git -C "$directory" fetch --quiet origin deploy || return 2
    local_commit=$(git -C "$directory" rev-parse HEAD) || return 2
    remote_commit=$(git -C "$directory" rev-parse origin/deploy) || return 2

    if [[ "$local_commit" == "$remote_commit" ]]; then
        log "$name: sem alterações."
        return 10
    fi

    git -C "$directory" pull --ff-only origin deploy || return 2
    log "$name: atualizado de ${local_commit:0:7} para ${remote_commit:0:7}."
    return 0
}

verify_services() {
    local service
    local running_services
    local missing_services=()
    local required_services=(postgres backend frontend)

    running_services=$(docker compose -f "$COMPOSE_FILE" ps --status running --services)

    for service in "${required_services[@]}"; do
        if ! grep -qx "$service" <<< "$running_services"; then
            missing_services+=("$service")
        fi
    done

    if ((${#missing_services[@]} > 0)); then
        log "Serviços não iniciados: ${missing_services[*]}"
        return 1
    fi

    log "Serviços obrigatórios em execução: ${required_services[*]}."
}

if [[ "${1:-}" == "--test-email" ]]; then
    send_email "Teste de e-mail - Com Amor IF" "O envio de e-mail do monitor de deploy foi configurado com sucesso."
    exit 0
fi

if [[ "${1:-}" != "" ]]; then
    echo "Uso: $0 [--test-email]" >&2
    exit 1
fi

changed_repositories=()

if sync_repository "Backend" "$BACKEND_DIR"; then
    changed_repositories+=("backend")
else
    result=$?
    [[ "$result" == "10" ]] || deployment_error "Não foi possível atualizar o repositório backend."
fi

if sync_repository "Frontend" "$FRONTEND_DIR"; then
    changed_repositories+=("frontend")
else
    result=$?
    [[ "$result" == "10" ]] || deployment_error "Não foi possível atualizar o repositório frontend."
fi

if ((${#changed_repositories[@]} == 0)); then
    log "Nenhuma atualização encontrada."
    exit 0
fi

log "Atualização encontrada em: ${changed_repositories[*]}. Iniciando rebuild."
docker compose -f "$COMPOSE_FILE" up --build --detach
verify_services

message="Deploy realizado com sucesso.

Repositórios atualizados: ${changed_repositories[*]}
Data: $(date '+%Y-%m-%d %H:%M:%S %z')
Log: $LOG_FILE"
send_email "Deploy realizado - Com Amor IF" "$message" || true
