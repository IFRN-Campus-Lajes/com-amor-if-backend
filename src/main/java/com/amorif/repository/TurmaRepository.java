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
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turma t where t.id = :id")
    java.util.Optional<Turma> findByIdForUpdate(@Param("id") Long id);

	
	List<Turma> findAllByAnoLetivo(AnoLetivo anoLetivo);
	
	List<Turma> findAllByTurno(Integer turno);

	List<Turma> findAllByTurnoAndAnoLetivo(Integer turno, AnoLetivo anoLetivo);
	
}
