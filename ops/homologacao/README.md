# Homologação local — issue #5 do backend

Este ambiente integra o processamento automático do backend com o patch de
auditoria do frontend. Não corresponde à issue #5 do frontend (gerenciamento de
sensos e consolidação da Área do Mago), cuja implementação já estava na base.

## Origem do código

- Backend: branch `codex/backend-issue-5-regras-automaticas`, criada sobre
  `codex/issue-32-contrato-sensos` em `4465c56`, preservando as alterações locais.
- Frontend: branch `codex/backend-issue-5-auditoria`, criada sobre
  `codex/issue-5-area-mago` em `cb2c788`, preservando o patch e a correção do ESLint.
- As branches foram alinhadas à `main` antes dos PRs. Nenhuma branch anterior foi reescrita.

## Subir

Com o backend empacotado (`bash mvnw package -DskipTests`, JDK 21) e os dois
repositórios lado a lado:

```bash
docker compose -f ops/homologacao/compose.yml up -d --build
```

Após a inicialização completa do backend, prepare os cenários fictícios:

```bash
docker compose -f ops/homologacao/compose.yml exec -T postgres \
  psql -U amorif_homolog -d amorif_homolog -v ON_ERROR_STOP=1 \
  < ops/homologacao/cenarios.sql
```

Abra http://localhost:3105/homologacao/entrar para entrar como administrador
fictício e acessar a auditoria. O adaptador local simula apenas o SUAP; emissão,
registro, validação do JWT e autorização continuam no backend real. Não informe
credenciais SUAP. O ambiente só publica a porta 3105 em 127.0.0.1.

O backend usa `dev,homologacao`, com catálogo de demonstração e disparo
**a cada minuto** (`AMORIF_AUTOMATICAS_CRON=0 * * * * *`), no segundo zero.
O padrão de produção continua mensal (`0 0 3 1 * *`), sem alteração.
Os disparos aparecem na auditoria com origem Mensal e solicitante Sistema,
pois executam a mesma rotina de produção. Use Atualizar para acompanhar.
O botão Reprocessar continua disponível para consolidação manual.
O perfil dev recria o esquema quando o backend reinicia: dados e auditoria são
descartáveis. Reaplique os cenários após reiniciar. Não use credenciais ou dados
reais; os Dockerfiles não copiam os arquivos .env dos repositórios.

## Cenários

O SQL limpa as pontuações de demonstração apenas deste banco e prepara o
primeiro bimestre. Veja os nomes e resultados esperados no próprio SQL.
Após preparar os cenários, reprocessar o **1º bimestre** cria dois bônus para
HOMOLOG A; repetir cria zero. HOMOLOG B permanece impedida, e o bônus anulado
de HOMOLOG C permanece anulado. A auditoria registra ambas as execuções.
O **2º bimestre** fica disponível para testar manualmente: a primeira execução
cria um bônus de limpeza para HOMOLOG A; repetir cria zero.
Novos bônus continuam pendentes de aprovação.

## Encerrar

```bash
docker compose -f ops/homologacao/compose.yml down
```

O volume Docker permanece. Para remover os dados fictícios, acrescente `-v`.
Não há publicação de imagens nem alteração de ambiente remoto.
