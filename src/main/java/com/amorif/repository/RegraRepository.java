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

	List<Regra> findByCategoriaIgnoreCaseAndSenso_Id(String categoria, Long sensoId);

	boolean existsByCategoriaIgnoreCase(String categoria);

	boolean existsByCategoriaIgnoreCaseAndSenso_Id(String categoria, Long sensoId);

	boolean existsByCategoriaIgnoreCaseAndSenso_IdAndAtivoTrue(String categoria, Long sensoId);

	Regra findTopByCodigoVersaoOrderByVersaoDesc(String codigoVersao);

	@Query("select distinct r.categoria from Regra r where r.categoria is not null and trim(r.categoria) <> ''")
	List<String> findDistinctCategorias();

}
