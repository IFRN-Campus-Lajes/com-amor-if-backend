package com.amorif.services.automaticas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import com.amorif.exceptions.ClosedSchoolYearException;

@Configuration
@EnableScheduling
@Profile("prod")
@ConditionalOnProperty(name = "amorif.automaticas.enabled", havingValue = "true", matchIfMissing = true)
public class PontuacaoAutomaticaScheduler {
    private static final Logger log = LoggerFactory.getLogger(PontuacaoAutomaticaScheduler.class);
    private final ExecucaoAutomaticaService service;
    public PontuacaoAutomaticaScheduler(ExecucaoAutomaticaService service) { this.service = service; }

    @Scheduled(cron = "${amorif.automaticas.cron:0 0 3 1 * *}", zone = "America/Fortaleza")
    public void executar() {
        try {
            var resultado = service.executar(null, "MENSAL", "Sistema");
            int criados = resultado.pontuacoes().size();
            log.info("BONUS confirmado | origem=mensal criados={}", criados);
        } catch (ClosedSchoolYearException ex) {
            log.info("BONUS ignorado | {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("BONUS falha | transação revertida; reprocessar pela rota administrativa", ex);
        }
    }
}
