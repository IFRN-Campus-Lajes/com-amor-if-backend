package com.amorif.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amorif.entities.Olimpiada;

import jakarta.persistence.LockModeType;

public interface OlimpiadaRepository extends JpaRepository<Olimpiada, Long> {

	List<Olimpiada> findAllByOrderByNomeAsc();

	boolean existsByNomeIgnoreCase(String nome);

	boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT o FROM Olimpiada o WHERE o.id = :id")
	Optional<Olimpiada> findByIdForUpdate(@Param("id") Long id);
}
