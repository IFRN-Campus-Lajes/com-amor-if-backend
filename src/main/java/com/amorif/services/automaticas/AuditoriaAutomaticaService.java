package com.amorif.services.automaticas;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.amorif.entities.ExecucaoAutomatica;
import com.amorif.repository.ExecucaoAutomaticaRepository;

/** Transações independentes: a falha das pontuações não apaga a auditoria. */
@Service
public class AuditoriaAutomaticaService {
    private final ExecucaoAutomaticaRepository repository;
    private final jakarta.persistence.EntityManager entityManager;
    public AuditoriaAutomaticaService(ExecucaoAutomaticaRepository repository,
            jakarta.persistence.EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExecucaoAutomatica iniciar(String origem, String solicitante, Integer bimestre) {
        ExecucaoAutomatica execucao = new ExecucaoAutomatica();
        execucao.setId(UUID.randomUUID());
        execucao.setOrigem(origem);
        execucao.setSolicitante(solicitante);
        execucao.setBimestre(bimestre);
        execucao.setStatus("EM_EXECUCAO");
        execucao.setInicio(Instant.now());
        return repository.saveAndFlush(execucao);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAno(UUID id, Long anoId, Integer ano) {
        ExecucaoAutomatica execucao = repository.findById(id).orElseThrow();
        execucao.setAnoLetivoId(anoId);
        execucao.setAno(ano);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ExecucaoAutomatica concluir(UUID id, String status, int criados, int existentes, int inelegiveis, String erro) {
        ExecucaoAutomatica execucao = repository.findById(id).orElseThrow();
        // O contexto HTTP pode conter a versão anterior a registrarAno, que
        // confirma em REQUIRES_NEW. Recarregue antes de concluir para não
        // sobrescrever o snapshot do ano com os valores antigos (nulos).
        entityManager.refresh(execucao);
        execucao.setStatus(status);
        execucao.setFim(Instant.now());
        execucao.setCriados(criados);
        execucao.setExistentes(existentes);
        execucao.setInelegiveis(inelegiveis);
        execucao.setErro(erro == null ? null : erro.substring(0, Math.min(1000, erro.length())));
        return repository.saveAndFlush(execucao);
    }
}
