package com.amorif.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.amorif.entities.AnoLetivo;

/**
 * @author osailton
 */

public interface AnoLetivoRepository extends JpaRepository<AnoLetivo, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AnoLetivo a where a.aberto = true order by a.ano desc limit 1")
    AnoLetivo getLastActiveAnoLetivoForUpdate();

	
	// Get the last active AnoLetivo
	@Query(value = "SELECT a FROM AnoLetivo a "
			+ "WHERE a.aberto = true "
			+ "ORDER BY a.ano DESC "
			+ "LIMIT 1")
	AnoLetivo getLastActiveAnoLetivo();
	
	// Get open AnoLetivo by Id
	@Query(value = "SELECT a FROM AnoLetivo a "
			+ "WHERE a.aberto = true "
			+ "AND a.id = :id")
	Optional<AnoLetivo> getOpenAnoLetivoById(Long id);

}
