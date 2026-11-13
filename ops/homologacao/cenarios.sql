-- Exclusivo do PostgreSQL fictício criado por este compose.
-- 1º bimestre: turma 1 recebe dois bônus; turma 2 tem limpeza impeditiva;
-- turma 3 já tem bônus de limpeza anulado e não deve receber outro.
-- 2º bimestre: turma 1 pode receber bônus de limpeza ainda inexistente.
BEGIN;
DELETE FROM pontuacao;
DELETE FROM execucao_automatica;
UPDATE turma SET nome = 'HOMOLOG A — Elegível' WHERE id = 1;
UPDATE turma SET nome = 'HOMOLOG B — Impedida' WHERE id = 2;
UPDATE turma SET nome = 'HOMOLOG C — Bônus anulado' WHERE id = 3;
INSERT INTO pontuacao (contador, turma_id, ano_id, regra_id, created_by, bimestre, pontos, aplicado, anulado, data, motivacao)
SELECT row_number() OVER (PARTITION BY t.id ORDER BY s.id, period.bimestre), t.id, t.ano_letivo_id, r.id,
       (SELECT min(id) FROM amorif_usuario), period.bimestre, 10, true, false, now(), 'Avaliação excelente fictícia de homologação'
FROM turma t
JOIN senso s ON s.descricao IN ('Limpeza', 'Ordenação')
JOIN regra r ON r.senso_id = s.id AND r.descricao IN ('10 pontos pela limpeza excelente', '10 pontos pela organização excelente', '10 pontos por limpeza excelente', '10 pontos por organização excelente')
CROSS JOIN (VALUES (0), (1)) AS period(bimestre)
WHERE (period.bimestre = 0 AND t.id IN (1, 2, 3) AND (t.id = 1 OR s.descricao = 'Limpeza'))
   OR (period.bimestre = 1 AND t.id = 1 AND s.descricao = 'Limpeza');
INSERT INTO pontuacao (contador, turma_id, ano_id, regra_id, created_by, bimestre, pontos, aplicado, anulado, data, motivacao)
SELECT 10, t.id, t.ano_letivo_id, r.id, (SELECT min(id) FROM amorif_usuario), 0, 5, true, false, now(), 'Avaliação impeditiva fictícia de homologação'
FROM turma t JOIN regra r ON r.descricao IN ('5 pontos pela limpeza mediana', '5 pontos por limpeza mediana')
WHERE t.id = 2;
INSERT INTO pontuacao (contador, turma_id, ano_id, regra_id, created_by, bimestre, pontos, aplicado, anulado, data, motivacao)
SELECT 10, t.id, t.ano_letivo_id, r.id, (SELECT min(id) FROM amorif_usuario), 0, 20, false, true, now(), 'Bônus fictício previamente anulado'
FROM turma t JOIN regra r ON r.descricao LIKE '20 pontos%' JOIN senso s ON s.id = r.senso_id AND s.descricao = 'Limpeza'
WHERE t.id = 3;
COMMIT;
