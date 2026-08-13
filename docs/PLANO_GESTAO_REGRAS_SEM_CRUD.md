# Plano de gestão de regras sem CRUD

Este documento define como inserir, editar, desativar ou excluir regras enquanto
o sistema não possui um CRUD administrativo. O objetivo é manter o catálogo do
código e o banco de produção consistentes, preservar pontuações históricas e
evitar que uma reinicialização recrie ou sobrescreva regras de forma inesperada.

## Fonte de verdade

Enquanto não houver CRUD, a fonte de verdade do catálogo é composta por:

- `ProdConfig.java`: valores, operação, senso, tipo, permissões e estado ativo;
- `TestConfig.java`: equivalente usado no ambiente de teste;
- `RegraCategorias.java`: descrição canônica, categoria e aliases históricos;
- testes do sincronizador e do catálogo.

Na inicialização, o sincronizador procura uma regra existente por senso e por
descrição canônica ou alias. Quando encontra, preserva o ID e atualiza descrição,
grupo, categoria, estado ativo, limites, senso, tipo e funções. Uma alteração
manual nesses campos pode ser desfeita na próxima inicialização se não tiver sido
refletida no código. A operação `SUM`/`SUB` não pode ser trocada pelo
sincronizador, porque isso reinterpretaria pontuações históricas.

## Classificação da mudança

Antes de implementar, classificar a solicitação em uma destas categorias:

1. **Correção de apresentação:** redação ou categoria muda, mas o significado e
   a forma de pontuar permanecem iguais. Deve preservar o registro e seu ID.
2. **Ajuste operacional compatível:** limites, tipo ou funções mudam sem alterar
   o significado das pontuações já lançadas. Exige análise e teste direcionado.
3. **Mudança semântica:** operação, unidade, frequência, vínculo com aluno ou
   interpretação da pontuação muda. Criar uma nova regra e desativar a antiga.
4. **Nova regra:** não há equivalente histórico. Inserir pelo catálogo em código.
5. **Desativação:** a regra deixa de aceitar lançamentos, mas seu histórico deve
   continuar disponível.
6. **Exclusão:** permitida somente quando não existe pontuação referenciando a
   regra e quando ela também foi retirada do catálogo para não ser recriada.

Em caso de dúvida entre ajuste compatível e mudança semântica, tratar como
mudança semântica.

## Processo para inserir uma regra

1. Registrar a justificativa, o senso, a descrição, a categoria, a operação, os
   limites, o tipo de regra, as funções autorizadas e o ciclo de vigência.
2. Verificar se não existe regra ativa ou histórica com o mesmo significado.
3. Adicionar a construção da regra em `ProdConfig.java` e `TestConfig.java`.
4. Adicionar a descrição canônica e a categoria em `RegraCategorias.java`. Uma
   regra realmente nova não deve receber aliases de outra regra.
5. Criar testes que validem descrição, categoria, limites, tipo, funções e
   idempotência da sincronização.
6. Executar a suíte completa e revisar o diff em pull request.
7. Gerar snapshot do banco e implantar o backend. A inicialização criará a regra
   sem depender de um ID previamente escolhido.
8. Consultar o banco e validar a regra na tela antes de liberar lançamentos.

IDs são gerados pelo banco e não devem ser fixados no código do catálogo.

## Processo para editar uma regra existente

### Correção de apresentação

1. Manter a descrição anterior como alias da mesma definição.
2. Alterar a descrição canônica ou a categoria.
3. Testar que o sincronizador preserva o ID existente.
4. Implantar e confirmar que existe somente uma regra ativa correspondente.

Um registro histórico inativo não deve ser cadastrado como alias de uma regra
ativa com significado diferente. Por exemplo, a regra histórica “Perda de 1
ponto por dia de suspensão de aluno” não é alias da regra ativa de 5 pontos.

### Ajuste de limites, tipo ou funções

1. Consultar a quantidade e uma amostra dos lançamentos históricos.
2. Confirmar que o novo valor não muda a interpretação do que já foi lançado.
3. Alterar `ProdConfig.java` e `TestConfig.java` e adicionar um teste que simule
   o registro existente com a configuração anterior.
4. Validar que o mesmo ID foi atualizado e que uma segunda sincronização não
   produz novas alterações.

Nunca trocar `SUM` por `SUB`, ou o inverso, em uma regra com histórico. Para
isso, desativar a regra antiga e criar outra.

## Processo para desativar uma regra

1. Manter a regra no catálogo e definir `ativo(false)` na construção canônica.
2. Não remover seus aliases enquanto houver instalações que precisem localizar
   o registro existente.
3. Confirmar que consultas de catálogo não retornam regras inativas e que os
   relatórios históricos continuam resolvendo a referência.
4. Não reutilizar a descrição da regra inativa como alias de uma nova regra com
   outro significado.

Remover uma regra do `ProdConfig` não a desativa no banco: regras ausentes do
catálogo não são modificadas pelo sincronizador.

## Processo para excluir uma regra

A exclusão deve ser excepcional. Antes dela:

```sql
SELECT r.id, r.descricao, r.ativo, count(p.regra_id) AS lancamentos
  FROM regra r
  LEFT JOIN pontuacao p ON p.regra_id = r.id
 WHERE r.id = :regra_id
 GROUP BY r.id, r.descricao, r.ativo;
```

Só prosseguir se `lancamentos = 0`. A mudança deve remover a regra de
`ProdConfig.java`, `TestConfig.java` e `RegraCategorias.java` na mesma entrega.
No banco, remover primeiro os vínculos de `regra_funcao` e depois a regra, sempre
em transação e com pré-condições que confiram ID e descrição.

## Auditoria obrigatória antes de uma entrega

Inventário com funções e quantidade de lançamentos:

```sql
SELECT r.id,
       s.descricao AS senso,
       r.descricao,
       r.categoria,
       r.ativo,
       r.operacao,
       r.valor_minimo,
       r.valor_maximo,
       tr.descricao AS tipo_regra,
       count(DISTINCT p.id) AS lancamentos,
       string_agg(DISTINCT f.name, ', ' ORDER BY f.name) AS funcoes
  FROM regra r
  JOIN senso s ON s.id = r.senso_id
  JOIN tipo_regra tr ON tr.id = r.tipo_regra_id
  LEFT JOIN pontuacao p ON p.regra_id = r.id
  LEFT JOIN regra_funcao rf ON rf.regra_id = r.id
  LEFT JOIN funcao f ON f.id = rf.role_id
 GROUP BY r.id, s.descricao, tr.descricao
 ORDER BY s.descricao, r.id;
```

Regras ativas sem categoria:

```sql
SELECT id, descricao
  FROM regra
 WHERE ativo = true
   AND categoria IS NULL;
```

Possíveis duplicidades ativas por senso e descrição normalizada:

```sql
SELECT senso_id,
       lower(regexp_replace(trim(descricao), '[[:space:]]+', ' ', 'g')) AS descricao_normalizada,
       count(*) AS quantidade,
       array_agg(id ORDER BY id) AS ids
  FROM regra
 WHERE ativo = true
 GROUP BY senso_id,
          lower(regexp_replace(trim(descricao), '[[:space:]]+', ' ', 'g'))
HAVING count(*) > 1;
```

As duas últimas consultas devem retornar zero linhas, salvo exceção analisada e
documentada.

## Alteração emergencial diretamente no banco

Uma edição direta só deve ocorrer quando esperar uma implantação normal causar
impacto operacional maior. Nesse caso:

1. Preparar e testar primeiro a alteração equivalente no código.
2. Deixar a nova imagem pronta, mas ainda não iniciada.
3. Gerar snapshot restaurável do banco.
4. Parar todas as instâncias do backend.
5. Executar no pgAdmin um script versionado e revisado, contendo `BEGIN`, locks,
   pré-condições, alteração, pós-condições e `COMMIT`.
6. Nunca executar apenas parte do script nem continuar após uma exceção.
7. Iniciar o novo backend e repetir as consultas de auditoria.
8. Só então iniciar ou liberar o frontend.
9. Remover do repositório o script específico do incidente depois de concluída
   a entrega, mantendo este procedimento permanente e o histórico no Git.

Se o banco for alterado antes do código, não reiniciar a versão antiga do
backend: a sincronização poderá sobrescrever a mudança ou recriar regras.

## Rollback e evidências

- Antes do `COMMIT`, qualquer falha deve abortar a transação inteira.
- Depois do `COMMIT`, uma inconsistência exige backend parado e restauração do
  snapshot, salvo se houver script de reversão previamente revisado.
- Guardar no pull request: justificativa, consulta anterior, script ou diff,
  testes executados, consulta posterior e responsável pela validação funcional.
- Validar no mínimo: catálogo por função, lançamento dentro e fora dos limites,
  ausência de duplicidades, relatórios históricos e reinicialização idempotente.

## Critério para substituir este processo

O CRUD futuro deve implementar controle de acesso, auditoria de alterações,
vigência, desativação em vez de exclusão para regras com histórico, validação de
limites e tipos, proteção contra mudança de operação e transações equivalentes
às descritas neste documento. Até isso ocorrer, toda alteração de regra passa
por código, revisão e implantação controlada.
