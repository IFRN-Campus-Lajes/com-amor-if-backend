package com.amorif.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amorif.entities.AnoLetivo;
import com.amorif.entities.Turma;

/**
 * @author osailton 
 */
public interface TurmaRepository extends JpaRepository<Turma, Long> {
	
	List<Turma> findAllByAnoLetivo(AnoLetivo anoLetivo);
	
	List<Turma> findAllByTurno(Integer turno);

	List<Turma> findAllByTurnoAndAnoLetivo(Integer turno, AnoLetivo anoLetivo);
	
	@Query("SELECT t FROM Turma t " +
		       "JOIN Pontuacao p ON p.turma.id = t.id " +
		       "WHERE p.bimestre = :bimestre " +
		       "AND p.anoLetivo = :anoLetivo " +
		       "AND p.regra.id = :regraId " +
		       "AND NOT EXISTS (" +
		       "  SELECT 1 FROM Pontuacao p2 " +
		       "  WHERE p2.turma.id = t.id " +
		       "  AND p2.bimestre = :bimestre " +
		       "  AND p2.regra.id IN (:outrasRegrasIds)" +
		       "  AND p2.anulado = false" +
		       ") " +
		       "GROUP BY t.id " +
		       "HAVING COUNT(p) > 0")
		List<Turma> findTurmasQualificadasParaBonus(@Param("regraId") Long regraId, 
		                                            @Param("outrasRegrasIds") List<Long> outrasRegrasIds, 
		                                            @Param("bimestre") Integer bimestre,
		                                            @Param("anoLetivo") AnoLetivo anoLetivo);


}
