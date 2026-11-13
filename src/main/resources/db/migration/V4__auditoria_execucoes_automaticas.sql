CREATE TABLE execucao_automatica (
    id UUID PRIMARY KEY,
    origem VARCHAR(24) NOT NULL,
    status VARCHAR(24) NOT NULL,
    solicitante VARCHAR(255) NOT NULL,
    ano_letivo_id BIGINT,
    ano INTEGER,
    bimestre INTEGER,
    inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    fim TIMESTAMP WITH TIME ZONE,
    criados INTEGER NOT NULL DEFAULT 0,
    existentes INTEGER NOT NULL DEFAULT 0,
    inelegiveis INTEGER NOT NULL DEFAULT 0,
    erro VARCHAR(1000),
    CONSTRAINT ck_execucao_bimestre CHECK (bimestre IS NULL OR bimestre BETWEEN 0 AND 3),
    CONSTRAINT ck_execucao_status CHECK (status IN ('EM_EXECUCAO', 'SUCESSO', 'FALHA', 'IGNORADA')),
    CONSTRAINT ck_execucao_origem CHECK (origem IN ('MENSAL', 'ADMINISTRATIVA'))
);

-- Identificadores e nome do solicitante são snapshots: a auditoria não é apagada
-- nem invalida quando o cadastro de um ano ou usuário muda.
CREATE INDEX idx_execucao_automatica_inicio ON execucao_automatica (inicio DESC, id DESC);
