-- Hotfix do catálogo de regras — 2026-08-13
--
-- Execução prevista: pgAdmin, com o backend parado e após snapshot do banco.
-- Execute o arquivo inteiro. Qualquer divergência nas pré-condições aborta a
-- transação sem aplicar parcialmente as alterações.

BEGIN;

LOCK TABLE regra IN SHARE ROW EXCLUSIVE MODE;
LOCK TABLE regra_funcao IN SHARE ROW EXCLUSIVE MODE;
LOCK TABLE pontuacao IN SHARE ROW EXCLUSIVE MODE;

ALTER TABLE regra
    ADD COLUMN IF NOT EXISTS ativo boolean;

UPDATE regra
SET ativo = true
WHERE ativo IS NULL;

ALTER TABLE regra
    ALTER COLUMN ativo SET DEFAULT true,
    ALTER COLUMN ativo SET NOT NULL;

DO $$
DECLARE
    descricao_regra text;
    quantidade_lancamentos bigint;
BEGIN
    SELECT descricao
      INTO descricao_regra
      FROM regra
     WHERE id = 37;

    IF descricao_regra IS NULL THEN
        RAISE EXCEPTION 'Pré-condição inválida: a regra histórica ID 37 não existe.';
    END IF;

    IF descricao_regra NOT IN (
        '1 ponto por dia de aluno da turma suspenso',
        'Perda de 1 ponto por dia de suspensão do aluno',
        'Perda de 5 pontos por dia de suspensão do aluno'
    ) THEN
        RAISE EXCEPTION 'Pré-condição inválida: descrição inesperada para a regra ID 37: %', descricao_regra;
    END IF;

    SELECT count(*)
      INTO quantidade_lancamentos
      FROM pontuacao
     WHERE regra_id = 37;

    IF quantidade_lancamentos <> 1 THEN
        RAISE EXCEPTION 'Pré-condição inválida: a regra ID 37 deveria ter 1 lançamento, mas possui %.', quantidade_lancamentos;
    END IF;

    SELECT descricao
      INTO descricao_regra
      FROM regra
     WHERE id = 31;

    IF descricao_regra IS NULL THEN
        RAISE EXCEPTION 'Pré-condição inválida: a regra de projetos ID 31 não existe.';
    END IF;

    IF descricao_regra NOT IN (
        'Pontos por aluno da turma em cada bimestre por atuação em projetos de pesquisa/extensão',
        'Pontos por aluno da turma em projetos de pesquisa ou extensão no bimestre',
        'Pontos por alunos em projetos de pesquisa ou extensão'
    ) THEN
        RAISE EXCEPTION 'Pré-condição inválida: descrição inesperada para a regra ID 31: %', descricao_regra;
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM regra r
          JOIN tipo_regra tr ON tr.id = r.tipo_regra_id
         WHERE r.id = 31
           AND tr.descricao = 'Valor Variável por bimestre'
           AND tr.fixo = false
           AND tr.tem_aluno = false
           AND tr.frequencia = 1
    ) THEN
        RAISE EXCEPTION 'Pré-condição inválida: a regra ID 31 não está configurada como variável por bimestre e sem aluno.';
    END IF;

    SELECT count(*)
      INTO quantidade_lancamentos
      FROM pontuacao
     WHERE regra_id = 31;

    IF quantidade_lancamentos <> 12 THEN
        RAISE EXCEPTION 'Pré-condição inválida: a regra ID 31 deveria ter 12 lançamentos, mas possui %.', quantidade_lancamentos;
    END IF;

    IF EXISTS (SELECT 1 FROM regra WHERE id = 27) THEN
        SELECT count(*) INTO quantidade_lancamentos FROM pontuacao WHERE regra_id = 27;
        IF quantidade_lancamentos <> 0 THEN
            RAISE EXCEPTION 'A regra ID 27 não pode ser excluída: possui % lançamento(s).', quantidade_lancamentos;
        END IF;
        IF NOT EXISTS (
            SELECT 1 FROM regra
             WHERE id = 27
               AND descricao = '1 ponto por aluno da turma em cada bimestre por atuação de monitoria'
        ) THEN
            RAISE EXCEPTION 'Pré-condição inválida: descrição inesperada para a regra ID 27.';
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM regra WHERE id = 45) THEN
        SELECT count(*) INTO quantidade_lancamentos FROM pontuacao WHERE regra_id = 45;
        IF quantidade_lancamentos <> 0 THEN
            RAISE EXCEPTION 'A regra ID 45 não pode ser excluída: possui % lançamento(s).', quantidade_lancamentos;
        END IF;
        IF NOT EXISTS (
            SELECT 1 FROM regra
             WHERE id = 45
               AND descricao = '1 ponto por aluno em projeto de pesquisa ou extensão no bimestre'
        ) THEN
            RAISE EXCEPTION 'Pré-condição inválida: descrição inesperada para a regra ID 45.';
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM regra WHERE id = 49) THEN
        SELECT count(*) INTO quantidade_lancamentos FROM pontuacao WHERE regra_id = 49;
        IF quantidade_lancamentos <> 0 THEN
            RAISE EXCEPTION 'A regra ID 49 não pode ser excluída: possui % lançamento(s).', quantidade_lancamentos;
        END IF;
        IF NOT EXISTS (
            SELECT 1 FROM regra
             WHERE id = 49
               AND descricao = '5 pontos por premiação de aluno da turma na Expotec do campus'
        ) THEN
            RAISE EXCEPTION 'Pré-condição inválida: descrição inesperada para a regra ID 49.';
        END IF;
    END IF;
END
$$;

UPDATE regra
   SET descricao = 'Perda de 5 pontos por dia de suspensão do aluno',
       categoria = 'Ocorrências disciplinares',
       ativo = false
 WHERE id = 37;

UPDATE regra
   SET descricao = 'Pontos por alunos em projetos de pesquisa ou extensão',
       categoria = 'Pesquisa, extensão e eventos',
       ativo = true
 WHERE id = 31;

DELETE FROM regra_funcao
 WHERE regra_id IN (27, 45, 49);

DELETE FROM regra
 WHERE id IN (27, 45, 49);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM regra WHERE id IN (27, 45, 49)) THEN
        RAISE EXCEPTION 'Pós-condição inválida: uma das regras 27, 45 ou 49 não foi excluída.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM regra
         WHERE id = 37
           AND ativo = false
           AND categoria = 'Ocorrências disciplinares'
    ) THEN
        RAISE EXCEPTION 'Pós-condição inválida: a regra ID 37 não foi desativada corretamente.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM regra
         WHERE id = 31
           AND ativo = true
           AND categoria = 'Pesquisa, extensão e eventos'
    ) THEN
        RAISE EXCEPTION 'Pós-condição inválida: a regra ID 31 não foi atualizada corretamente.';
    END IF;
END
$$;

COMMIT;

-- Resultado esperado imediatamente após o script e antes do novo backend:
SELECT id, descricao, categoria, ativo
  FROM regra
 WHERE id IN (27, 31, 37, 45, 49)
 ORDER BY id;

-- A consulta abaixo também roda imediatamente e, nesse momento, ainda não
-- mostrará a regra canônica de Expotec. Salve-a e execute-a novamente somente
-- depois de iniciar o novo backend. No segundo uso, o resultado esperado é:
-- 1) IDs 27, 45 e 49 ausentes;
-- 2) ID 31 ativa e categorizada;
-- 3) ID 37 inativa e preservada;
-- 4) uma única regra ativa de Expotec, criada a partir do catálogo canônico.
SELECT r.id,
       s.descricao AS senso,
       r.descricao,
       r.categoria,
       r.ativo,
       r.operacao,
       r.valor_minimo,
       r.valor_maximo,
       tr.descricao AS tipo_regra
  FROM regra r
  JOIN senso s ON s.id = r.senso_id
  JOIN tipo_regra tr ON tr.id = r.tipo_regra_id
 WHERE lower(r.descricao) LIKE '%monitor%'
    OR lower(r.descricao) LIKE '%suspens%'
    OR lower(r.descricao) LIKE '%projeto%'
    OR lower(r.descricao) LIKE '%expotec%'
    OR lower(r.descricao) LIKE '%eventos do campus%'
 ORDER BY s.descricao, r.id;
