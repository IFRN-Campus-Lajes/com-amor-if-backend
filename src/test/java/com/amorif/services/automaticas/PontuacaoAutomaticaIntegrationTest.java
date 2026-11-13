package com.amorif.services.automaticas;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.amorif.entities.*;
import com.amorif.repository.*;
import com.amorif.exceptions.*;

@DataJpaTest(showSql = false, properties = {"spring.datasource.url=${test.database.url:jdbc:h2:mem:bonus;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000}",
    "spring.datasource.driver-class-name=${test.database.driver:org.h2.Driver}", "spring.datasource.username=${test.database.username:sa}", "spring.datasource.password=${test.database.password:}",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.properties.hibernate.dialect=${test.database.dialect:org.hibernate.dialect.H2Dialect}",
    "spring.flyway.enabled=false", "spring.config.import="})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PontuacaoAutomaticaIntegrationTest.Config.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PontuacaoAutomaticaIntegrationTest {
    @Configuration
    @EnableAutoConfiguration
    @EntityScan("com.amorif.entities")
    @EnableJpaRepositories("com.amorif.repository")
    @Import({PontuacaoAutomaticaService.class, AuditoriaAutomaticaService.class, ExecucaoAutomaticaService.class})
    static class Config {}

    @Autowired PontuacaoAutomaticaService service;
    @Autowired PontuacaoRepository pontos;
    @Autowired AnoLetivoRepository anos;
    @Autowired TurmaRepository turmas;
    @Autowired EntityManager em;
    @Autowired jakarta.persistence.EntityManagerFactory entityManagerFactory;
    @Autowired PlatformTransactionManager manager;
    TransactionTemplate tx;
    Long anoId, turmaId, bonusId, excelenteId, ruimId;

    @BeforeEach void preparar() {
        tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> {
            for (String entity : List.of("ExecucaoAutomatica", "Pontuacao", "Turma", "Regra", "CategoriaRegra", "TipoRegra", "Senso", "User", "Role", "AnoLetivo")) {
                // Remoção por entidade também limpa as tabelas de associação.
                em.createQuery("from " + entity, Object.class).getResultList().forEach(em::remove);
                em.flush();
            }
            Role role = Role.builder().name("ROLE_SISTEMA").build(); em.persist(role);
            Senso senso = Senso.builder().descricao("Ordenação").build(); em.persist(senso);
            TipoRegra manual = TipoRegra.builder().descricao("Manual").frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).fixo(true).build(); em.persist(manual);
            TipoRegra automatico = TipoRegra.builder().descricao("Automático").fixo(true).automatico(true)
                .frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build(); em.persist(automatico);
            AnoLetivo ano = AnoLetivo.builder().ano(2026).aberto(true).build(); em.persist(ano); anoId = ano.getId();
            Turma turma = new Turma(); turma.setNome("Turma A"); turma.setAnoLetivo(ano); turma.setTurno(0);
            em.persist(turma); turmaId = turma.getId();
            BonusBimestral condicao = BonusBimestral.DEFINICOES.getFirst();
            excelenteId = regra(condicao.excelente(), senso, manual, 10).getId();
            for (String descricao : condicao.impeditivas()) {
                Regra regra = regra(descricao, senso, manual, 0);
                if (ruimId == null || descricao.equals(condicao.impeditivas().getFirst())) ruimId = regra.getId();
            }
            bonusId = regra("20 pontos por 100% de avaliações excelentes no bimestre", senso, automatico, 20).getId();
            avaliacao(excelenteId, 0, true, false);
        });
    }

    private Regra regra(String descricao, Senso senso, TipoRegra tipo, int valor) {
        Regra regra = Regra.builder().descricao(descricao).senso(senso).tipoRegra(tipo).valorMinimo(valor)
            .operacao("SUM").roles(List.of()).build(); em.persist(regra); return regra;
    }
    private Pontuacao avaliacao(Long regraId, int bimestre, boolean aplicado, boolean anulado) {
        Turma turma = em.find(Turma.class, turmaId);
        Integer contador = pontos.contadorByTurma(turma);
        Pontuacao p = Pontuacao.builder().contador(contador == null ? 1 : contador + 1).turma(turma)
            .anoLetivo(em.find(AnoLetivo.class, anoId)).regra(em.find(Regra.class, regraId)).bimestre(bimestre)
            .pontos(10).aplicado(aplicado).anulado(anulado).build(); em.persist(p); em.flush(); return p;
    }
    private long bonusCount() {
        return tx.execute(status -> pontos.findAll().stream().filter(p -> p.getRegra().getId().equals(bonusId)).count());
    }

    @Autowired ExecucaoAutomaticaService executor;
    @Autowired ExecucaoAutomaticaRepository execucoes;

    @Test void auditoriaRegistraSucessoComContadoresEAutoria() {
        var resultado = executor.executar(null, "ADMINISTRATIVA", "administrador");
        var auditoria = execucoes.findById(resultado.execucao().getId()).orElseThrow();
        assertEquals("SUCESSO", auditoria.getStatus());
        assertEquals(1, auditoria.getCriados());
        assertEquals(3, auditoria.getInelegiveis());
        assertEquals(anoId, auditoria.getAnoLetivoId());
        assertEquals(2026, auditoria.getAno());
        assertEquals("administrador", auditoria.getSolicitante());
        assertNotNull(auditoria.getFim());
        var repetida = executor.executar(null, "MENSAL", "Sistema");
        assertEquals(0, repetida.execucao().getCriados());
        assertEquals(1, repetida.execucao().getExistentes());
        assertEquals(2, execucoes.count());
    }

    @Test void auditoriaPreservaAnoComContextoAbertoDuranteRequisicaoHttp() {
        // Simula o OpenEntityManagerInView padrão do Spring Web: a execução
        // iniciada permanece no contexto enquanto registrarAno usa REQUIRES_NEW.
        EntityManager contexto = entityManagerFactory.createEntityManager();
        org.springframework.transaction.support.TransactionSynchronizationManager.bindResource(
            entityManagerFactory, new org.springframework.orm.jpa.EntityManagerHolder(contexto));
        java.util.UUID id;
        try {
            var resultado = executor.executar(0, "ADMINISTRATIVA", "admin-http");
            id = resultado.execucao().getId();
            assertEquals(2026, resultado.execucao().getAno());
            assertEquals(anoId, resultado.execucao().getAnoLetivoId());
        } finally {
            org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource(entityManagerFactory);
            contexto.close();
        }
        var persistida = execucoes.findById(id).orElseThrow();
        assertEquals(2026, persistida.getAno());
        assertEquals(anoId, persistida.getAnoLetivoId());
    }

    @Test void auditoriaPreservaFalhaMesmoComRollbackENovaTentativaFunciona() {
        tx.executeWithoutResult(status -> {
            em.find(Regra.class, excelenteId).setDescricao("Condição removida");
        });
        assertThrows(InvalidArgumentException.class, () -> executor.executar(0, "ADMINISTRATIVA", "admin"));
        assertEquals(0, bonusCount());
        var falha = execucoes.findAll().getFirst();
        assertEquals("FALHA", falha.getStatus());
        assertNotNull(falha.getFim());
        assertNotNull(falha.getErro());
        assertEquals(anoId, falha.getAnoLetivoId());
        tx.executeWithoutResult(status -> em.find(Regra.class, excelenteId)
            .setDescricao(BonusBimestral.DEFINICOES.getFirst().excelente()));
        assertEquals("SUCESSO", executor.executar(0, "ADMINISTRATIVA", "admin").execucao().getStatus());
        assertEquals(2, execucoes.count());
        assertEquals("FALHA", execucoes.findById(falha.getId()).orElseThrow().getStatus());
    }

    @Test void semAnoAbertoRegistraExecucaoIgnorada() {
        tx.executeWithoutResult(status -> em.find(AnoLetivo.class, anoId).setAberto(false));
        var resultado = executor.executar(null, "MENSAL", "Sistema");
        assertEquals("IGNORADA", resultado.execucao().getStatus());
        assertNotNull(resultado.execucao().getErro());
        assertEquals(0, resultado.execucao().getCriados());
    }

    @Test void impedimentoUsaTurmaEBimestreSemFiltroAdicionalDoAnoDaPontuacao() {
        tx.executeWithoutResult(status -> {
            var antigo = AnoLetivo.builder().ano(2025).aberto(false).build(); em.persist(antigo);
            avaliacao(ruimId, 0, true, false).setAnoLetivo(antigo);
        });
        assertTrue(service.processar(null).isEmpty());
    }

    @Test void concedeTambemBonusDeLimpezaComDescricoesLegadas() {
        tx.executeWithoutResult(status -> {
            Senso limpeza = Senso.builder().descricao("Limpeza").build(); em.persist(limpeza);
            TipoRegra automatico = em.find(Regra.class, bonusId).getTipoRegra();
            TipoRegra manual = em.find(Regra.class, excelenteId).getTipoRegra();
            var condicao = BonusBimestral.DEFINICOES.get(1);
            Regra excelente = regra("10 pontos pela limpeza excelente", limpeza, manual, 10);
            condicao.impeditivas().forEach(descricao -> regra(descricao, limpeza, manual, 0));
            regra("20 pontos ao fim de cada bimestre se a turma tiver 100% de nota 10", limpeza, automatico, 20);
            avaliacao(excelente.getId(), 0, true, false);
        });
        assertEquals(2, service.processar(null).size());
        assertTrue(service.processar(null).isEmpty());
    }

    @Test void configuracaoDesconhecidaFalhaSemConcederBonus() {
        tx.executeWithoutResult(status -> {
            Regra existente = em.find(Regra.class, bonusId);
            regra("Bônus sem algoritmo", existente.getSenso(), existente.getTipoRegra(), 50);
        });
        assertThrows(IllegalStateException.class, () -> service.processar(null));
        assertEquals(0, bonusCount());
    }

    @Test void concedeSemUsuarioLogadoENaoDuplicaMesmoAnulado() {
        var criadas = service.processar(null);
        assertEquals(1, criadas.size());
        assertFalse(criadas.getFirst().isAplicado());
        assertEquals(PontuacaoAutomaticaService.MATRICULA_SISTEMA, criadas.getFirst().getUser().getMatricula());
        assertTrue(service.processar(null).isEmpty());
        tx.executeWithoutResult(status -> pontos.findAll().stream().filter(p -> p.getRegra().getId().equals(bonusId))
            .forEach(p -> p.setAnulado(true)));
        assertTrue(service.processar(null).isEmpty());
        assertEquals(1, bonusCount());
    }

    @Test void reprocessaNovasAvaliacoesDeTodosOsBimestresEPreservaBonusDesfavoravel() {
        service.processar(null);
        tx.executeWithoutResult(status -> { avaliacao(ruimId, 0, true, false); avaliacao(excelenteId, 3, true, false); });
        assertEquals(1, service.processar(null).size());
        assertEquals(2, bonusCount());
    }

    @Test void pendentesEAnuladasNaoQualificamNemImpedem() {
        tx.executeWithoutResult(status -> {
            avaliacao(ruimId, 0, false, false); avaliacao(ruimId, 0, false, true);
            avaliacao(excelenteId, 1, false, false); avaliacao(excelenteId, 2, true, true);
        });
        assertEquals(1, service.processar(null).size());
    }

    @Test void impeditivaAprovadaBloqueiaAteSerAnulada() {
        tx.executeWithoutResult(status -> avaliacao(ruimId, 0, true, false));
        assertTrue(service.processar(null).isEmpty());
        tx.executeWithoutResult(status -> pontos.findAll().stream().filter(p -> p.getRegra().getId().equals(ruimId))
            .forEach(p -> p.setAnulado(true)));
        assertEquals(1, service.processar(null).size());
    }

    @Test void concorrenciaNaoDuplica() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Callable<Integer> disparo = () -> { inicio.await(); return service.processar(null).size(); };
            Future<Integer> a = pool.submit(disparo), b = pool.submit(disparo); inicio.countDown();
            assertEquals(1, a.get(15, TimeUnit.SECONDS) + b.get(15, TimeUnit.SECONDS));
            assertEquals(1, bonusCount());
        } finally { pool.shutdownNow(); }
    }

    @Test void falhaDepoisDeGravarReverteTudoEPermiteReprocessar() {
        // Força falha depois de o serviço inserir o bônus, ainda na transação externa.
        assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(status -> {
            assertEquals(1, service.processar(null).size());
            throw new IllegalStateException("Falha simulada antes do commit");
        }));
        assertEquals(0, bonusCount());
        assertEquals(1, service.processar(null).size());
    }

    @Test void anoFechadoEBimestreExtraNaoSaoProcessados() {
        assertThrows(InvalidBimesterException.class, () -> service.processar(4));
        tx.executeWithoutResult(status -> em.find(AnoLetivo.class, anoId).setAberto(false));
        assertThrows(ClosedSchoolYearException.class, () -> service.processar(null));
        assertEquals(0, bonusCount());
    }

    @Test void bonusHistoricoAnuladoComOutroIdBloqueiaNovaConcessao() {
        tx.executeWithoutResult(status -> {
            Regra atual = em.find(Regra.class, bonusId);
            atual.setCodigoVersao("bonus-ordenacao");
            Regra antiga = regra("20 pontos ao fim de cada bimestre se a turma tiver 100% de nota 10",
                atual.getSenso(), atual.getTipoRegra(), 20);
            antiga.setAtivo(false); antiga.setCodigoVersao("bonus-ordenacao");
            avaliacao(antiga.getId(), 0, false, true);
        });
        assertTrue(service.processar(null).isEmpty());
    }
}
