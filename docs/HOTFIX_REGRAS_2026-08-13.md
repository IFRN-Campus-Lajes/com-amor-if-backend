# Hotfix do catálogo de regras — 2026-08-13

## Ordem de execução

1. Deixar os artefatos do backend e do frontend prontos para implantação, sem iniciá-los.
2. Gerar um snapshot restaurável do banco de produção.
3. Parar todas as instâncias do backend.
4. Confirmar que não existe sessão executando o `ProdConfig`.
5. No pgAdmin, abrir e executar por inteiro `hotfix-regras-2026-08-13.sql`.
6. Conferir o primeiro resultado exibido após o `COMMIT`.
7. Implantar e iniciar o backend do hotfix.
8. Executar novamente a consulta final do arquivo SQL.
9. Implantar o frontend do hotfix.
10. Executar a validação funcional abaixo.

## Validação funcional

- A regra ID 37 não aparece para novos lançamentos.
- A pontuação histórica ligada à ID 37 continua nos relatórios antigos.
- A ID 42 é a única regra de monitoria disponível.
- A ID 31 é a única regra de projetos disponível, está em `Pesquisa, extensão e eventos` e aceita valor variável.
- Existe uma única regra de Expotec: `10 a 140 pontos para as turmas com maior pontuação em eventos do campus`.
- Não há regra ativa com `categoria` nula.
- As abas aparecem em `Utilização`, `Ordenação`, `Limpeza`, `Saúde` e `Autodisciplina`.

## Consulta de integridade

```sql
SELECT r.id, r.descricao, r.categoria, r.ativo, count(p.regra_id) AS lancamentos
  FROM regra r
  LEFT JOIN pontuacao p ON p.regra_id = r.id
 GROUP BY r.id, r.descricao, r.categoria, r.ativo
 ORDER BY r.id;
```

O resultado não deve conter regras ativas com categoria nula:

```sql
SELECT id, descricao
  FROM regra
 WHERE ativo = true
   AND categoria IS NULL;
```

## Rollback

Se o SQL falhar antes do `COMMIT`, o PostgreSQL desfaz toda a transação. Se o
problema ocorrer depois do `COMMIT`, manter o backend parado e restaurar o
snapshot. Não iniciar uma versão antiga do backend sobre o banco já migrado,
pois o catálogo antigo pode recriar regras removidas.
