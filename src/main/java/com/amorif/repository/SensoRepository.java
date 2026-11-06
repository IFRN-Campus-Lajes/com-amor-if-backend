package com.amorif.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amorif.entities.Senso;

public interface SensoRepository extends JpaRepository<Senso, Long> {
	
	// Método para buscar um Senso pela descrição
    Senso findByDescricao(String descricao);

	Optional<Senso> findByDescricaoIgnoreCase(String descricao);

	List<Senso> findAllByOrderByDescricaoAsc();
	
}
