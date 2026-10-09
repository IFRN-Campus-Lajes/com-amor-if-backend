package com.amorif.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amorif.entities.CategoriaRegra;
import com.amorif.entities.CategoriaRegraId;

public interface CategoriaRegraRepository extends JpaRepository<CategoriaRegra, CategoriaRegraId> {
    List<CategoriaRegra> findAllByOrderBySenso_DescricaoAscNomeAsc();
    Optional<CategoriaRegra> findByNomeIgnoreCaseAndSenso_Id(String nome, Long sensoId);
}
