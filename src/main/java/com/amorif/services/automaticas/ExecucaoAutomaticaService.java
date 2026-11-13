package com.amorif.services.automaticas;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import com.amorif.entities.ExecucaoAutomatica;
import com.amorif.entities.Pontuacao;
import com.amorif.exceptions.ClosedSchoolYearException;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.exceptions.InvalidBimesterException;

@Service
public class ExecucaoAutomaticaService {
    private static final Logger log = LoggerFactory.getLogger(ExecucaoAutomaticaService.class);
    private final PontuacaoAutomaticaService processor;
    private final AuditoriaAutomaticaService auditoria;
    public record Resultado(ExecucaoAutomatica execucao, List<Pontuacao> pontuacoes) {}

    public ExecucaoAutomaticaService(PontuacaoAutomaticaService processor, AuditoriaAutomaticaService auditoria) {
        this.processor = processor;
        this.auditoria = auditoria;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Pontuacao> processar(Integer bimestre) {
        return executar(bimestre, "ADMINISTRATIVA", SecurityContextHolder.getContext()
                .getAuthentication().getName()).pontuacoes();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Resultado executar(Integer bimestre, String origem, String solicitante) {
        // Requisições inválidas não constituem execução de cálculo.
        if (bimestre != null && (bimestre < 0 || bimestre > 3))
            throw new InvalidBimesterException("Selecione um dos quatro bimestres comuns.");
        ExecucaoAutomatica execucao = auditoria.iniciar(origem, solicitante, bimestre);
        PontuacaoAutomaticaService.Apuracao resultado;
        try {
            resultado = processor.apurar(bimestre, execucao.getId());
        } catch (ClosedSchoolYearException ex) {
            ExecucaoAutomatica ignorada = auditoria.concluir(execucao.getId(), "IGNORADA", 0, 0, 0, ex.getMessage());
            return new Resultado(ignorada, List.of());
        } catch (RuntimeException ex) {
            auditoria.concluir(execucao.getId(), "FALHA", 0, 0, 0,
                    "Não foi possível concluir a apuração. Consulte os logs com o identificador da execução.");
            log.error("BONUS falha | execucao={} origem={}", execucao.getId(), origem, ex);
            throw new InvalidArgumentException("Falha na execução " + execucao.getId()
                    + ". Nenhum bônus desta execução foi gravado. Consulte a auditoria.");
        }
        // O processamento já foi confirmado. Falha ao atualizar auditoria não deve
        // ser reportada como rollback de pontuações: permanece EM_EXECUCAO para investigação.
        ExecucaoAutomatica concluida = auditoria.concluir(execucao.getId(), "SUCESSO",
            resultado.pontuacoes().size(), resultado.existentes(), resultado.inelegiveis(), null);
        return new Resultado(concluida, resultado.pontuacoes());
    }
}
