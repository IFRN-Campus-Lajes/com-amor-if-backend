#!/usr/bin/env bash
# Banco exclusivo e descartável: não lê credenciais da aplicação.
set -euo pipefail
cd "$(dirname "$0")/.."
container="amorif-issue5-test-$$"
cleanup() { docker rm -f "$container" >/dev/null 2>&1 || true; }
trap cleanup EXIT

docker info >/dev/null
docker run -d --rm --name "$container" \
  -e POSTGRES_USER=amorif_test -e POSTGRES_PASSWORD=amorif_test \
  -e POSTGRES_DB=amorif_issue5 -p 127.0.0.1::5432 postgres:16-alpine >/dev/null
ready=false
for ((attempt=0; attempt<30; attempt++)); do
  if docker exec "$container" pg_isready -U amorif_test -d amorif_issue5 >/dev/null 2>&1; then
    ready=true
    break
  fi
  sleep 1
done
if [[ "$ready" != true ]]; then
  docker logs "$container"
  exit 1
fi
port=$(docker port "$container" 5432/tcp)
port=${port##*:}
bash mvnw -Dtest=PontuacaoAutomaticaIntegrationTest \
  "-Dtest.database.url=jdbc:postgresql://127.0.0.1:$port/amorif_issue5" \
  -Dtest.database.driver=org.postgresql.Driver \
  -Dtest.database.username=amorif_test -Dtest.database.password=amorif_test \
  -Dtest.database.dialect=org.hibernate.dialect.PostgreSQLDialect test

# O teste acima usa create-drop; confira também o DDL real da nova migração.
docker exec -i "$container" psql -U amorif_test -d amorif_issue5 -v ON_ERROR_STOP=1 \
  < src/main/resources/db/migration/V4__auditoria_execucoes_automaticas.sql
