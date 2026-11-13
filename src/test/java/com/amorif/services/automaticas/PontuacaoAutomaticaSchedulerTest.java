package com.amorif.services.automaticas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;
import com.amorif.exceptions.ClosedSchoolYearException;

class PontuacaoAutomaticaSchedulerTest {
    @Test void registraAgendamentoApenasQuandoHabilitado() {
        for (String perfil : List.of("prod", "homologacao", "dev", "test")) {
            for (boolean habilitado : List.of(true, false)) {
                try (var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
                    context.getEnvironment().setActiveProfiles(perfil);
                    context.getEnvironment().getPropertySources().addFirst(
                        new org.springframework.core.env.MapPropertySource("teste",
                            java.util.Map.of("amorif.automaticas.enabled", String.valueOf(habilitado))));
                    context.registerBean(ExecucaoAutomaticaService.class, () -> mock(ExecucaoAutomaticaService.class));
                    context.register(PontuacaoAutomaticaScheduler.class);
                    context.refresh();
                    boolean esperado = habilitado && perfil.equals("prod");
                    assertEquals(esperado, !context.getBeansOfType(PontuacaoAutomaticaScheduler.class).isEmpty());
                    if (esperado) assertEquals(1, context.getBean(
                        org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor.class)
                        .getScheduledTasks().size());
                }
            }
        }
    }

    @Test void cronConfiguravelPreservaPadraoMensalDeProducao() {
        for (boolean personalizado : List.of(false, true)) {
            try (var context = new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
                context.getEnvironment().setActiveProfiles("prod");
                if (personalizado) {
                    context.getEnvironment().getPropertySources().addFirst(
                        new org.springframework.core.env.MapPropertySource("cron-teste",
                            java.util.Map.of("amorif.automaticas.cron", "0 * * * * *")));
                }
                context.registerBean(ExecucaoAutomaticaService.class, () -> mock(ExecucaoAutomaticaService.class));
                context.register(PontuacaoAutomaticaScheduler.class);
                context.refresh();
                var tasks = context.getBean(org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor.class)
                    .getScheduledTasks();
                var cron = (org.springframework.scheduling.config.CronTask) tasks.iterator().next().getTask();
                assertEquals(personalizado ? "0 * * * * *" : "0 0 3 1 * *", cron.getExpression());
            }
        }
    }

    @Test void disparoMensalReprocessaTodosOsBimestres() {
        var service = mock(ExecucaoAutomaticaService.class);
        when(service.executar(null, "MENSAL", "Sistema")).thenReturn(new ExecucaoAutomaticaService.Resultado(new com.amorif.entities.ExecucaoAutomatica(), List.of()));
        new PontuacaoAutomaticaScheduler(service).executar();
        verify(service).executar(null, "MENSAL", "Sistema");
        assertEquals(LocalDateTime.of(2027, 1, 1, 3, 0), CronExpression.parse("0 0 3 1 * *")
            .next(LocalDateTime.of(2026, 12, 31, 23, 59)));
    }
    @Test void falhaNaoImpedeProximoDisparo() {
        var service = mock(ExecucaoAutomaticaService.class);
        when(service.executar(null, "MENSAL", "Sistema")).thenThrow(new IllegalStateException("Falha simulada")).thenReturn(new ExecucaoAutomaticaService.Resultado(new com.amorif.entities.ExecucaoAutomatica(), List.of()));
        var scheduler = new PontuacaoAutomaticaScheduler(service);
        assertDoesNotThrow(scheduler::executar);
        assertDoesNotThrow(scheduler::executar);
        verify(service, times(2)).executar(null, "MENSAL", "Sistema");
    }
    @Test void semAnoAbertoIgnoraDisparo() {
        var service = mock(ExecucaoAutomaticaService.class);
        when(service.executar(null, "MENSAL", "Sistema")).thenThrow(new ClosedSchoolYearException("Fechado"));
        assertDoesNotThrow(new PontuacaoAutomaticaScheduler(service)::executar);
    }
}
