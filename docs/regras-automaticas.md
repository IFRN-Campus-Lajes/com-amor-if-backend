# Regras automáticas — issue #5

## Decisões

A aplicação confia no bimestre informado pelos avaliadores. Não calcula datas de
início/fim, não fecha bimestres e não associa meses civis aos bimestres letivos.

Em produção, o backend executa mensalmente os quatro bimestres comuns (códigos
0 a 3) do último ano letivo aberto. O padrão é dia 1, às 03h, no fuso
America/Fortaleza. Nenhum cron, pacote ou serviço adicional é instalado na VM.
O agendador roda no container Java existente.

São processados os dois bônus históricos de limpeza e ordenação. Cada um exige
pelo menos uma avaliação excelente aprovada e nenhuma avaliação impeditiva
aprovada na mesma turma e bimestre. O ano aberto seleciona as turmas; não há
filtro adicional pelo ano da pontuação no histórico da turma. Avaliações pendentes e anuladas são
ignoradas. As impeditivas são as avaliações ruim, mediana e boa e a ocorrência
negativa de limpeza/ordenação, conforme as condições históricas.

Um bônus novo continua **pendente de aprovação**, como no fluxo anterior. A
concessão não implica aprovação imediata ou entrada automática no ranking.

Qualquer bônus existente, inclusive pendente ou anulado, impede nova criação.
Versões históricas e descrições legadas são reconhecidas. Novas avaliações
favoráveis podem gerar bônus antes inexistentes. Novas avaliações desfavoráveis
não anulam nem alteram bônus existentes. O administrador decide anulações e
recursos pelas operações de cancelar/aprovar existentes. A exclusão de bônus é
bloqueada para impedir que uma nova execução recrie um registro excluído.

## Implementação

- `BonusBimestral`: condições explícitas, independentes do agendador e da gravação.
  Usa a correspondência de descrições canônicas/aliases de `RegraCategorias` e
  `codigoVersao` para reconhecer o catálogo legado e suas versões, sem IDs fixos.
  Esse uso é somente de leitura: não repopula ou sincroniza o catálogo.
- `PontuacaoAutomaticaService`: cálculo compartilhado pela rota e pelo agendador.
  Uma transação cobre cada chamada, incluindo a criação da identidade técnica.
  Uma trava pessimista no ano aberto serializa disparos entre instâncias e a
  rota administrativa. Uma trava na turma, também adotada pelo lançamento manual,
  protege o contador da chave composta de pontuação.
- `PontuacaoAutomaticaScheduler`: disparo mensal apenas no perfil `prod`.
- A autoria é o usuário técnico `!sistema-regras-automaticas`, criado na primeira
  execução com o perfil existente `ROLE_SISTEMA`. Não é necessário simular login
  no SecurityContext. Os logs administrativos identificam quem pediu a execução.
- Uma regra automática desconhecida ou condição histórica não encontrada causa
  erro explícito e rollback, em vez de conceder pontos com critérios presumidos.
- A consulta administrativa preserva todas as regras. Criação, edição, conversão
  para tipo automático e mudança de status de automáticas são rejeitadas na API.
  O lançamento manual comum também não aceita regras automáticas.

O banco continua sendo a fonte de verdade dos registros e dos valores. As
condições de concessão permanecem no código. A migração `V4__auditoria_execucoes_automaticas.sql` cria o histórico de execuções.

## Configuração no Docker existente

As propriedades são opcionais; os padrões já habilitam a execução em produção:

```yaml
services:
  backend:
    environment:
      AMORIF_AUTOMATICAS_ENABLED: "true"
      AMORIF_AUTOMATICAS_CRON: "0 0 3 1 * *"
```

Para desabilitar o agendador, use `AMORIF_AUTOMATICAS_ENABLED=false`. Isso não
remove a operação administrativa de reprocessamento. O cron do Spring tem seis
campos, incluindo segundos. Não é necessário mudar o Dockerfile.

O agendamento não é persistente. Se o container estiver parado no horário,
execute o reprocessamento administrativo ao restabelecer o serviço, ou aguarde o
próximo disparo mensal, que examinará novamente todos os bimestres do ano aberto.
Uma falha também pode ser retomada pela rota; não há tentativas contínuas nem
recuperação de anos já fechados. Nenhum disparo ocorre automaticamente ao subir
o container.

## Reprocessamento administrativo

Permanece restrito a `ROLE_ADMINISTRADOR`:

```http
POST /api/pontuacao/lancarPontosAutomaticos
Authorization: Bearer <token>
Content-Type: application/json

{"bimestre": 0}
```

O corpo `{}` ou `{"bimestre": null}` processa os quatro bimestres. Valores fora
 de 0–3 são rejeitados, incluindo o bimestre extra (4). A resposta mantém a lista
de pontuações criadas, agora com autoria Sistema. Nenhum parâmetro permite
forçar recriação ou ignorar anulações.

Os logs `BONUS` registram início, identificador da execução, ano, período,
turma, regra, contador, totais e confirmação após retorno transacional. Falhas
registram a exceção e a origem. A tabela `execucao_automatica` registra origem, solicitante, ano, período, início,
fim, status e totais. A auditoria usa transações independentes para preservar
falhas mesmo quando as pontuações sofrem rollback. Uma interrupção abrupta pode
deixar status EM_EXECUCAO para investigação; isso não impede reprocessamento.

## Auditoria administrativa

- `GET /api/admin/regras-automaticas/execucoes?page=0&size=20`: histórico paginado,
  mais recentes primeiro, no máximo 50 registros por página.
- `POST /api/admin/regras-automaticas/reprocessar` com `{}` ou `{"bimestre": 0}`:
  retorna a execução registrada. Restrito ao administrador, assim como a listagem.
- Status: EM_EXECUCAO, SUCESSO, FALHA e IGNORADA (sem ano aberto).
- Totais existentes/inelegíveis contam combinações de turma, regra e bimestre.

## Área do Mago

A página `/wizard-area/automatic-rules` apresenta o histórico e o disparo de
reprocessamento, com acesso abaixo da descrição da Área do Mago e link de retorno na auditoria.

As automáticas aparecem com “Automática · Somente consulta”, sem botões de
edição ou ativação/desativação. O tipo automático não aparece na criação de
regras. A implementação e os testes estão no repositório
[com-amor-if-frontend](https://github.com/IFRN-Campus-Lajes/com-amor-if-frontend/pull/34).

O histórico usa cartões em telas abaixo de 768 px e tabela nas demais.
Formulário, controles de toque, paginação e navegação foram validados nas
larguras de 320, 375, 390, 768 e 1024 px.

A API bloqueia as alterações mesmo antes da atualização do frontend.

## Validação

Testes cobrem concessão sem login, quatro períodos, ausência de ano aberto,
bimestre extra, pendências/anulações, impeditivas, preservação de bônus,
repetição, versões históricas, chamadas simultâneas, rollback e reprocessamento,
além dos bloqueios administrativos e do disparo mensal. A integração usa H2 em
modo PostgreSQL, incluindo travas reais e transações independentes concorrentes.
A suíte também foi validada em PostgreSQL 16 real. As propriedades
`test.database.url`, `test.database.driver`, `test.database.username`,
`test.database.password` e `test.database.dialect` permitem selecionar o banco
da integração. A suíte usa `create-drop`: execute somente em banco descartável.
A execução padrão de `bash mvnw test` continua usando H2.

Antes de publicar, conferir os aliases das regras herdadas e validar o
reprocessamento no ambiente de homologação.

## Backlog separado

1. Calendário letivo, fechamento e reabertura dos bimestres, com tratamento de
   lançamentos tardios e fechamento do ano.
2. Cadastro/edição de condições de concessão na Área do Mago: modelo persistido,
   tipos de cálculo, versionamento das condições, vigência e efeito sobre bônus
   históricos. Só então liberar a gestão de regras automáticas.
3. Retomada de disparos perdidos e alertas de falha. A implementação atual
   oferece auditoria persistida, logs e reprocessamento seguro.

## Histórico

Anteriormente, a rota administrativa selecionava turmas por IDs fixos e usava o
usuário logado para criar pontuações. A consulta de qualificação não filtrava
avaliações aprovadas; a presença do bônus entre os conflitos impedia algumas
repetições, mas sem proteção transacional contra disparos simultâneos. O fluxo
atual preserva a aprovação administrativa e torna a concessão mensal autônoma.
