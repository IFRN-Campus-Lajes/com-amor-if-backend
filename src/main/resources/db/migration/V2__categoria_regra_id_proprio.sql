-- Categoria passa a ter identidade estável. Nome e senso continuam únicos,
-- mas deixam de compor a chave primária para permitir renomeações simples.
ALTER TABLE categoria_regra ADD COLUMN IF NOT EXISTS id BIGINT;

CREATE SEQUENCE IF NOT EXISTS categoria_regra_id_seq;
ALTER SEQUENCE categoria_regra_id_seq OWNED BY categoria_regra.id;
ALTER TABLE categoria_regra ALTER COLUMN id SET DEFAULT NEXTVAL('categoria_regra_id_seq');

UPDATE categoria_regra
SET id = NEXTVAL('categoria_regra_id_seq')
WHERE id IS NULL;

SELECT SETVAL(
    'categoria_regra_id_seq',
    GREATEST(COALESCE((SELECT MAX(id) FROM categoria_regra), 1), 1),
    TRUE
);

ALTER TABLE categoria_regra ALTER COLUMN id SET NOT NULL;
ALTER TABLE categoria_regra DROP CONSTRAINT IF EXISTS pk_categoria_regra;
ALTER TABLE categoria_regra ADD CONSTRAINT pk_categoria_regra PRIMARY KEY (id);

-- O índice case-insensitive criado na V1 preserva a regra de negócio:
-- um nome não se repete dentro do mesmo senso.
CREATE UNIQUE INDEX IF NOT EXISTS uk_categoria_regra_nome_senso_ci
    ON categoria_regra (LOWER(nome), senso_id);

-- Associação estável para as regras. A coluna textual permanece como retrato
-- compatível com consultas e lançamentos históricos.
ALTER TABLE regra ADD COLUMN IF NOT EXISTS categoria_id BIGINT;

UPDATE regra r
SET categoria_id = c.id
FROM categoria_regra c
WHERE r.categoria_id IS NULL
  AND r.senso_id = c.senso_id
  AND LOWER(TRIM(r.categoria)) = LOWER(TRIM(c.nome));

ALTER TABLE regra DROP CONSTRAINT IF EXISTS fk_regra_categoria;
ALTER TABLE regra ADD CONSTRAINT fk_regra_categoria
    FOREIGN KEY (categoria_id) REFERENCES categoria_regra(id);

CREATE INDEX IF NOT EXISTS idx_regra_categoria_id ON regra (categoria_id);
