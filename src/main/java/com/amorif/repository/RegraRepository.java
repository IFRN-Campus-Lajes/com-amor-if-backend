package com.amorif.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amorif.entities.Regra;
import com.amorif.entities.Role;

public interface RegraRepository extends JpaRepository<Regra, Long> {

	List<Regra> findByAtivoTrue();

	List<Regra> findByRolesInAndAtivoTrue(List<Role> roles);

}
