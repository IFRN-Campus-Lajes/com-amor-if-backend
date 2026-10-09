package com.amorif.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amorif.entities.CategoriaRegra;

public interface CategoriaRegraRepository extends JpaRepository<CategoriaRegra, Long> {
    List<CategoriaRegra> findAllByOrderByNomeAsc();
    Optional<CategoriaRegra> findByNomeIgnoreCase(String nome);
}
