package com.amorif.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.amorif.entities.Regra;
import com.amorif.entities.Role;

public interface RegraRepository extends JpaRepository<Regra, Long> {

	List<Regra> findByAtivoTrue();

	List<Regra> findByRolesInAndAtivoTrue(List<Role> roles);

	List<Regra> findAllByOrderBySenso_DescricaoAscCategoriaAscDescricaoAsc();

	boolean existsByCategoriaIgnoreCase(String categoria);

	@Query("select distinct r.categoria from Regra r where r.categoria is not null and trim(r.categoria) <> ''")
	List<String> findDistinctCategorias();

}
