-- Identificador compartilhado pelas pontuacoes geradas em um mesmo
-- lancamento coletivo por turno. Registros historicos permanecem nulos.
ALTER TABLE pontuacao
    ADD COLUMN IF NOT EXISTS grupo_lancamento_id UUID;

CREATE INDEX IF NOT EXISTS idx_pontuacao_grupo_lancamento
    ON pontuacao (grupo_lancamento_id);
