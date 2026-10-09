-- Estrutura de versionamento das regras existentes.
-- As colunas começam opcionais, recebem os valores legados e só então ganham
-- as restrições necessárias para que a atualização seja segura em tabelas cheias.
ALTER TABLE regra ADD COLUMN IF NOT EXISTS codigo_versao VARCHAR(255);
ALTER TABLE regra ADD COLUMN IF NOT EXISTS versao INTEGER;
ALTER TABLE regra ADD COLUMN IF NOT EXISTS criado_por VARCHAR(255);
ALTER TABLE regra ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(255);
ALTER TABLE regra ADD COLUMN IF NOT EXISTS criado_em TIMESTAMP;
ALTER TABLE regra ADD COLUMN IF NOT EXISTS atualizado_em TIMESTAMP;

UPDATE regra
SET versao = COALESCE(versao, 1),
    codigo_versao = COALESCE(codigo_versao, MD5(RANDOM()::TEXT || CLOCK_TIMESTAMP()::TEXT || id::TEXT)),
    criado_em = COALESCE(criado_em, CURRENT_TIMESTAMP),
    atualizado_em = COALESCE(atualizado_em, CURRENT_TIMESTAMP);

ALTER TABLE regra ALTER COLUMN versao SET DEFAULT 1;
ALTER TABLE regra ALTER COLUMN versao SET NOT NULL;

-- O catálogo atual usa uma chave composta. O mesmo nome pode existir em
-- sensos diferentes, mas não pode se repetir dentro de um mesmo senso.
-- Este bloco também absorve uma eventual tabela criada por uma versão de
-- homologação anterior, que ainda utilizava um id numérico.
DO $$
BEGIN
    IF TO_REGCLASS('categoria_regra') IS NOT NULL
       AND EXISTS (
           SELECT 1 FROM information_schema.columns
           WHERE table_schema = CURRENT_SCHEMA()
             AND table_name = 'categoria_regra'
             AND column_name = 'id'
       ) THEN
        ALTER TABLE categoria_regra RENAME TO categoria_regra_legacy;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS categoria_regra (
    nome VARCHAR(100) NOT NULL,
    senso_id BIGINT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_categoria_regra PRIMARY KEY (nome, senso_id),
    CONSTRAINT fk_categoria_regra_senso FOREIGN KEY (senso_id) REFERENCES senso(id)
);

-- regra.categoria permanece como fotografia textual da versão histórica.
-- O catálogo é reconstruído por senso sem alterar nenhuma regra existente.
INSERT INTO categoria_regra (nome, senso_id, ativo, criado_em, atualizado_em)
SELECT MIN(TRIM(r.categoria)), r.senso_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM regra r
WHERE r.categoria IS NOT NULL
  AND TRIM(r.categoria) <> ''
  AND r.senso_id IS NOT NULL
GROUP BY LOWER(TRIM(r.categoria)), r.senso_id
ON CONFLICT (nome, senso_id) DO NOTHING;

DO $$
BEGIN
    IF TO_REGCLASS('categoria_regra_legacy') IS NOT NULL THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = CURRENT_SCHEMA()
              AND table_name = 'categoria_regra_legacy'
              AND column_name = 'senso_id'
        ) THEN
            EXECUTE '
                INSERT INTO categoria_regra (nome, senso_id, ativo, criado_em, atualizado_em)
                SELECT nome, senso_id, ativo, criado_em, atualizado_em
                FROM categoria_regra_legacy
                WHERE senso_id IS NOT NULL
                ON CONFLICT (nome, senso_id) DO UPDATE
                SET ativo = EXCLUDED.ativo,
                    atualizado_em = EXCLUDED.atualizado_em';
        END IF;

        DROP TABLE categoria_regra_legacy;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_regra_codigo_versao ON regra (codigo_versao);
CREATE INDEX IF NOT EXISTS idx_categoria_regra_senso ON categoria_regra (senso_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_categoria_regra_nome_senso_ci
    ON categoria_regra (LOWER(nome), senso_id);
