package com.amorif.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.amorif.entities.ExecucaoAutomatica;

public interface ExecucaoAutomaticaRepository extends JpaRepository<ExecucaoAutomatica, UUID> {}
