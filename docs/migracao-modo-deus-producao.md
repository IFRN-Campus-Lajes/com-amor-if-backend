# Migração da Área do Mago em produção

## O que muda

- A tabela `regra` recebe os campos de versionamento e auditoria.
- A tabela `categoria_regra` passa a usar a chave primária composta `(nome, senso_id)`.
- Categorias de mesmo nome podem existir em sensos diferentes.
- Categorias recebem um `id` próprio; `(senso_id, nome)` permanece como unicidade de negócio.
- `regra.categoria` continua existindo como fotografia histórica da categoria daquela versão.
- O Flyway executa a migração antes de o Hibernate validar o esquema.
- Em produção, o Hibernate deixa de alterar o banco automaticamente (`ddl-auto: validate`).

## Passo a passo do deploy

1. Faça um backup completo do PostgreSQL e valide que ele pode ser restaurado.
2. Antes do deploy, consulte categorias repetidas apenas por diferença entre maiúsculas e minúsculas:

   ```sql
   SELECT LOWER(TRIM(categoria)) AS categoria_normalizada,
          senso_id,
          COUNT(DISTINCT TRIM(categoria)) AS variacoes
   FROM regra
   WHERE categoria IS NOT NULL AND TRIM(categoria) <> ''
   GROUP BY LOWER(TRIM(categoria)), senso_id
   HAVING COUNT(DISTINCT TRIM(categoria)) > 1;
   ```

   A migração escolhe uma grafia canônica para essas variações, sem alterar as regras históricas.

3. Confirme que todas as regras categorizadas têm senso:

   ```sql
   SELECT id, descricao, categoria
   FROM regra
   WHERE categoria IS NOT NULL
     AND TRIM(categoria) <> ''
     AND senso_id IS NULL;
   ```

   Corrija qualquer resultado antes de continuar; regras sem senso não podem gerar uma categoria válida.

4. Publique primeiro o backend. Na primeira inicialização com o perfil `prod`, o Flyway:

   - cria `flyway_schema_history` e registra o estado anterior como versão `0` quando necessário;
   - adiciona os campos de versionamento à tabela `regra` inicialmente sem restrições;
   - preenche regras existentes com `versao = 1`, código de versão e datas;
   - aplica `NOT NULL` em `regra.versao` somente depois do preenchimento;
   - cria `categoria_regra` com chave primária `(nome, senso_id)`;
   - importa as categorias a partir de `regra.categoria` e `regra.senso_id`;
   - converte uma eventual tabela de homologação que ainda tenha chave numérica;
   - cria índices de apoio.

5. Aguarde o backend iniciar completamente. O Hibernate executará apenas a validação do esquema; qualquer divergência interromperá a inicialização em vez de alterar silenciosamente o banco.
6. Verifique o registro da migração:

   ```sql
   SELECT installed_rank, version, description, success
   FROM flyway_schema_history
   ORDER BY installed_rank;
   ```

   As versões `1` e `2` devem aparecer com `success = true`.

7. Verifique a chave composta:

   ```sql
   SELECT conname, pg_get_constraintdef(oid)
   FROM pg_constraint
   WHERE conrelid = 'categoria_regra'::regclass
     AND contype = 'p';
   ```

   O resultado deve mostrar `PRIMARY KEY (nome, senso_id)`.

8. Compare regras e categorias migradas:

   ```sql
   SELECT COUNT(*) AS categorias
   FROM categoria_regra;

   SELECT COUNT(*) AS categorias_esperadas
   FROM (
       SELECT LOWER(TRIM(categoria)), senso_id
       FROM regra
       WHERE categoria IS NOT NULL
         AND TRIM(categoria) <> ''
         AND senso_id IS NOT NULL
       GROUP BY LOWER(TRIM(categoria)), senso_id
   ) catalogo;
   ```

   Os totais devem ser iguais, desconsiderando categorias novas que ainda não tenham regras.

9. Publique o frontend e execute o checklist funcional da Área do Mago.

## Rollback

- O backend anterior continua entendendo `regra.categoria`; as novas colunas e a tabela de catálogo são aditivas.
- Em caso de falha funcional, reverta a aplicação, mas não apague `flyway_schema_history` nem execute a migração ao contrário manualmente.
- Se a própria migração falhar, o PostgreSQL desfaz a transação do Flyway. Investigue o erro antes de uma nova tentativa.
- Para recuperação completa de dados, restaure o backup criado antes do passo 1.

## JDK utilizada

Os testes foram executados com Eclipse Temurin 21.0.12.1, instalado em:

```text
/Users/juniorcunha/.jdks/jdk-21.0.12.1+1/Contents/Home
```

No Eclipse, adicione esse caminho em `Preferences > Java > Installed JREs`, marque-o como padrão e confirme que o projeto usa `JavaSE-21`.
