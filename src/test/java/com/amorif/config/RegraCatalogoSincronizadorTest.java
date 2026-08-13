package com.amorif.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.amorif.entities.Regra;
import com.amorif.entities.Role;
import com.amorif.entities.Senso;
import com.amorif.entities.TipoRegra;

class RegraCatalogoSincronizadorTest {

    private final Senso utilizacao = Senso.builder().id(1L).descricao("Utilização").build();
    private final TipoRegra tipoFixo = TipoRegra.builder().id(1L).descricao("Valor Fixo").build();
    private final TipoRegra tipoBimestral = TipoRegra.builder().id(2L).descricao("Valor Fixo por bimestre").build();
    private final List<Role> roles = List.of(Role.builder().id(1L).name("ROLE_BIBLIOTECARIO").build());

    @Test
    void revisaoTextualPreservaIdDoRegistroExistente() {
        Regra existente = regraExistente(42L, "Pontos por livro emprestado (1 ponto por livro)", tipoFixo);
        Regra canonica = regraCanonica("Pontos por livro emprestado (1 ponto por livro)", tipoFixo);

        List<Regra> alteradas = RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica));

        assertThat(alteradas).containsExactly(existente);
        assertThat(existente.getId()).isEqualTo(42L);
        assertThat(existente.getDescricao()).isEqualTo("1 ponto por livro emprestado");
    }

    @Test
    void mudancaDeFrequenciaAtualizaMesmoRegistro() {
        Regra existente = regraExistente(43L,
                "20 pontos no bimestre extra para a turma que mais tiver formado grupos de estudo no ano letivo",
                tipoFixo);
        Regra canonica = regraCanonica(
                "20 pontos no bimestre extra para a turma que mais tiver formado grupos de estudo no ano letivo",
                tipoBimestral);

        List<Regra> alteradas = RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica));

        assertThat(alteradas).containsExactly(existente);
        assertThat(existente.getId()).isEqualTo(43L);
        assertThat(existente.getTipoRegra()).isSameAs(tipoBimestral);
        assertThat(existente.getDescricao()).doesNotContain("[TURBO]");
    }

    @Test
    void tornaPunicaoPorMauComportamentoAvulsaSemRecriarARegra() {
        Senso saude = Senso.builder().id(2L).descricao("Saúde").build();
        TipoRegra variavelBimestral = TipoRegra.builder().id(3L)
                .descricao("Valor Variável por bimestre").frequencia(1).build();
        TipoRegra variavelAvulsa = TipoRegra.builder().id(4L)
                .descricao("Valor Variável").frequencia(2).build();
        Regra existente = Regra.builder().id(48L)
                .descricao("0 a 15 pontos por bimestre por mau comportamento")
                .operacao("SUB").valorMinimo(0).valorMaximo(15)
                .senso(saude).tipoRegra(variavelBimestral).roles(roles).build();
        Regra canonica = Regra.builder()
                .descricao("0 a 15 pontos por bimestre por mau comportamento")
                .operacao("SUB").valorMinimo(0).valorMaximo(15)
                .senso(saude).tipoRegra(variavelAvulsa).roles(roles).build();
        RegraCategorias.aplicar(List.of(canonica));

        List<Regra> alteradas = RegraCatalogoSincronizador.sincronizar(
                List.of(existente), List.of(canonica));

        assertThat(alteradas).containsExactly(existente);
        assertThat(existente.getId()).isEqualTo(48L);
        assertThat(existente.getTipoRegra()).isSameAs(variavelAvulsa);
        assertThat(existente.getOperacao()).isEqualTo("SUB");
        assertThat(existente.getValorMinimo()).isZero();
        assertThat(existente.getValorMaximo()).isEqualTo(15);
    }

    @Test
    void mudancaDeValorAtualizaMesmoRegistro() {
        Senso saude = Senso.builder().id(2L).descricao("Saúde").build();
        Regra existente = Regra.builder().id(44L)
                .descricao("1 ponto por aluno da turma que realizar Avaliação Biomédica de Saúde")
                .operacao("SUM").valorMinimo(1).senso(saude).tipoRegra(tipoFixo).roles(roles).build();
        Regra canonica = Regra.builder()
                .descricao("1 ponto por aluno da turma que realizar Avaliação Biomédica de Saúde")
                .operacao("SUM").valorMinimo(2).senso(saude).tipoRegra(tipoFixo).roles(roles).build();
        RegraCategorias.aplicar(List.of(canonica));

        List<Regra> alteradas = RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica));

        assertThat(alteradas).containsExactly(existente);
        assertThat(existente.getId()).isEqualTo(44L);
        assertThat(existente.getValorMinimo()).isEqualTo(2);
    }

    @Test
    void sincronizacaoEIdempotente() {
        Regra existente = regraExistente(45L, "Pontos por livro emprestado (1 ponto por livro)", tipoFixo);
        Regra canonica = regraCanonica("Pontos por livro emprestado (1 ponto por livro)", tipoFixo);
        RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica));

        List<Regra> segundaSincronizacao = RegraCatalogoSincronizador.sincronizar(
                List.of(existente), List.of(canonica));

        assertThat(segundaSincronizacao).isEmpty();
    }

	@Test
	void reconheceRegraAgregadaDeProjetosEPreservaSeuId() {
		Senso saude = Senso.builder().id(2L).descricao("Saúde").build();
		TipoRegra variavelBimestral = TipoRegra.builder().id(3L)
				.descricao("Valor Variável por bimestre").frequencia(1).build();
		Regra existente = Regra.builder().id(31L)
				.descricao("Pontos por aluno da turma em cada bimestre por atuação em projetos de pesquisa/extensão")
				.operacao("SUM").valorMinimo(1).valorMaximo(500).senso(saude)
				.tipoRegra(variavelBimestral).roles(roles).build();
		Regra canonica = Regra.builder()
				.descricao("Pontos por aluno da turma em cada bimestre por atuação em projetos de pesquisa/extensão")
				.operacao("SUM").valorMinimo(1).valorMaximo(500).senso(saude)
				.tipoRegra(variavelBimestral).roles(roles).build();
		RegraCategorias.aplicar(List.of(canonica));

		List<Regra> alteradas = RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica));

		assertThat(alteradas).containsExactly(existente);
		assertThat(existente.getId()).isEqualTo(31L);
		assertThat(existente.getDescricao())
				.isEqualTo("Pontos por alunos em projetos de pesquisa ou extensão");
		assertThat(existente.getCategoria()).isEqualTo("Pesquisa, extensão e eventos");
		assertThat(existente.getTipoRegra()).isSameAs(variavelBimestral);
	}

	@Test
	void reativaRegraCanonicaSeElaEstiverInativa() {
		Regra existente = regraExistente(48L, "Pontos por livro emprestado (1 ponto por livro)", tipoFixo);
		existente.setAtivo(false);
		Regra canonica = regraCanonica("Pontos por livro emprestado (1 ponto por livro)", tipoFixo);

		List<Regra> alteradas = RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica));

		assertThat(alteradas).containsExactly(existente);
		assertThat(existente.isAtivo()).isTrue();
	}

    @Test
    void aceitaAliasComDiferencasDeEspacosEQuebraDeLinha() {
        Senso saude = Senso.builder().descricao("Saúde").build();
        Regra existente = Regra.builder().id(46L)
                .descricao("[TURBO] 15 a 100 Pontos por Campanhas Educativas\r\n")
                .senso(saude).roles(roles).build();
        Regra canonica = Regra.builder().descricao("[TURBO] 15 a 100 Pontos por Campanhas Educativas")
                .senso(saude).roles(roles).build();
        RegraCategorias.aplicar(List.of(canonica));

        List<Regra> alteradas = RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica));

        assertThat(alteradas).containsExactly(existente);
        assertThat(existente.getId()).isEqualTo(46L);
        assertThat(existente.getDescricao()).isEqualTo("50 a 300 pontos por divulgação dos processos seletivos");
    }

    @Test
    void bloqueiaMudancaDeOperacaoQueReinterpretariaPontuacoesHistoricas() {
        Regra existente = regraExistente(47L, "Pontos por livro emprestado (1 ponto por livro)", tipoFixo);
        Regra canonica = regraCanonica("Pontos por livro emprestado (1 ponto por livro)", tipoFixo);
        canonica.setOperacao("SUB");

        assertThatThrownBy(() -> RegraCatalogoSincronizador.sincronizar(List.of(existente), List.of(canonica)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pontuacoes historicas");
    }

    private Regra regraExistente(Long id, String descricao, TipoRegra tipo) {
        return Regra.builder().id(id).descricao(descricao).operacao("SUM").valorMinimo(1)
                .senso(utilizacao).tipoRegra(tipo).roles(roles).build();
    }

    private Regra regraCanonica(String descricao, TipoRegra tipo) {
        Regra regra = Regra.builder().descricao(descricao).operacao("SUM").valorMinimo(1)
                .senso(utilizacao).tipoRegra(tipo).roles(roles).build();
        RegraCategorias.aplicar(List.of(regra));
        return regra;
    }
}
