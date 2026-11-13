package com.amorif.repository;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import com.amorif.entities.*;

@DataJpaTest(showSql = false, properties = {
    "spring.datasource.url=${test.database.url:jdbc:h2:mem:bimonthly;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}",
    "spring.datasource.driver-class-name=${test.database.driver:org.h2.Driver}",
    "spring.datasource.username=${test.database.username:sa}", "spring.datasource.password=${test.database.password:}",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=${test.database.dialect:org.hibernate.dialect.H2Dialect}",
    "spring.flyway.enabled=false", "spring.config.import="})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = BimonthlyFrequencyIntegrationTest.Config.class)
class BimonthlyFrequencyIntegrationTest {
    @Configuration
    @EnableAutoConfiguration
    @EntityScan("com.amorif.entities")
    @EnableJpaRepositories("com.amorif.repository")
    static class Config {}

    @Autowired EntityManager em;
    @Autowired PontuacaoRepository pontos;
    AnoLetivo atual, anterior;
    Turma turma, outraTurma;
    User professor, outroProfessor;
    Regra regra, alternativa;

    @BeforeEach void preparar() {
        atual = AnoLetivo.builder().ano(2026).aberto(true).build(); em.persist(atual);
        anterior = AnoLetivo.builder().ano(2025).aberto(false).build(); em.persist(anterior);
        turma = Turma.builder().nome("Turma reutilizada").anoLetivo(atual).turno(0).build(); em.persist(turma);
        outraTurma = Turma.builder().nome("Outra turma").anoLetivo(atual).turno(0).build(); em.persist(outraTurma);
        professor = User.builder().matricula("professor-a").nome("Professor A").funcoes(Set.of()).build(); em.persist(professor);
        outroProfessor = User.builder().matricula("professor-b").nome("Professor B").funcoes(Set.of()).build(); em.persist(outroProfessor);
        Senso senso = Senso.builder().descricao("Saúde").build(); em.persist(senso);
        TipoRegra tipo = TipoRegra.builder().descricao("Bimestral").fixo(true)
            .frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build(); em.persist(tipo);
        regra = Regra.builder().descricao("Média igual").operacao("SUM").valorMinimo(8)
            .grupo("media_comparativa").tipoRegra(tipo).senso(senso).roles(List.of()).build(); em.persist(regra);
        alternativa = Regra.builder().descricao("Média maior").operacao("SUM").valorMinimo(20)
            .grupo("media_comparativa").tipoRegra(tipo).senso(senso).roles(List.of()).build(); em.persist(alternativa);
    }

    private void registrar(AnoLetivo ano, int bimestre, boolean anulado) {
        em.persist(Pontuacao.builder().contador(1).turma(turma).regra(regra).anoLetivo(ano)
            .user(professor).bimestre(bimestre).pontos(8).matriculaAluno("aluno-a")
            .aplicado(false).anulado(anulado).build());
        em.flush();
    }

    @Test void anoAnteriorNaoBloqueiaMesmaTurmaRegraGrupoEBimestre() {
        registrar(anterior, 0, false);
        assertFalse(pontos.existsByBimesterAndRule(atual.getId(), 0, regra.getId(), turma.getId()));
        assertFalse(pontos.existsByBimesterAndGroup(atual.getId(), 0, turma.getId(), regra.getGrupo(), professor));
        assertTrue(pontos.existsByBimesterAndRule(anterior.getId(), 0, regra.getId(), turma.getId()));
        assertTrue(pontos.existsByBimesterAndGroup(anterior.getId(), 0, turma.getId(), regra.getGrupo(), professor));
    }

    @Test void mesmoAnoBloqueiaRegraEQualquerAlternativaDoGrupoDoProfessor() {
        registrar(atual, 0, false);
        assertTrue(pontos.existsByBimesterAndRule(atual.getId(), 0, regra.getId(), turma.getId()));
        assertTrue(pontos.existsByBimesterAndGroup(atual.getId(), 0, turma.getId(), alternativa.getGrupo(), professor));
        assertFalse(pontos.existsByBimesterAndRule(atual.getId(), 0, alternativa.getId(), turma.getId()));
    }

    @Test void outroProfessorPodeUsarMesmoGrupoNoMesmoAnoTurmaEBimestre() {
        registrar(atual, 0, false);
        assertFalse(pontos.existsByBimesterAndGroup(atual.getId(), 0, turma.getId(), regra.getGrupo(), outroProfessor));
    }

    @Test void outroBimestreNaoBloqueia() {
        registrar(atual, 0, false);
        assertFalse(pontos.existsByBimesterAndRule(atual.getId(), 1, regra.getId(), turma.getId()));
        assertFalse(pontos.existsByBimesterAndGroup(atual.getId(), 1, turma.getId(), regra.getGrupo(), professor));
    }

    @Test void outraTurmaNaoBloqueia() {
        registrar(atual, 0, false);
        assertFalse(pontos.existsByBimesterAndRule(atual.getId(), 0, regra.getId(), outraTurma.getId()));
        assertFalse(pontos.existsByBimesterAndGroup(atual.getId(), 0, outraTurma.getId(), regra.getGrupo(), professor));
    }

    @Test void pontuacaoAnuladaNaoBloqueiaNovaConcessao() {
        registrar(atual, 0, true);
        assertFalse(pontos.existsByBimesterAndRule(atual.getId(), 0, regra.getId(), turma.getId()));
        assertFalse(pontos.existsByBimesterAndGroup(atual.getId(), 0, turma.getId(), regra.getGrupo(), professor));
    }

    @Test void consultasPorAlunoMantemContratoAnteriorSemFiltroDeAno() {
        registrar(anterior, 0, false);
        assertTrue(pontos.existsByBimesterAndRulePerStudent(0, regra.getId(), turma.getId(), "aluno-a"));
        assertTrue(pontos.existsByBimesterAndGroupPerStudent(0, turma.getId(), "aluno-a", regra.getGrupo()));
        assertFalse(pontos.existsByBimesterAndRulePerStudent(0, regra.getId(), turma.getId(), "aluno-b"));
        assertFalse(pontos.existsByBimesterAndGroupPerStudent(0, turma.getId(), "aluno-b", regra.getGrupo()));
    }
}
