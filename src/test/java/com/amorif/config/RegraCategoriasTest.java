package com.amorif.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.amorif.entities.Regra;
import com.amorif.entities.Senso;

class RegraCategoriasTest {

    @Test
    void aplicaCategoriasSemModificarOGrupoDeExclusividade() {
        Regra regraDeMedia = Regra.builder()
                .descricao("8 pontos pela média igual ao do bimestre anterior")
                .grupo("media_comparativa")
                .senso(Senso.builder().descricao("Saúde").build())
                .build();
        Regra regraDeLivro = Regra.builder()
                .descricao("Pontos por livro emprestado (1 ponto por livro)")
                .senso(Senso.builder().descricao("Utilização").build())
                .build();

        RegraCategorias.aplicar(List.of(regraDeMedia, regraDeLivro));

        assertThat(regraDeMedia.getCategoria()).isEqualTo("Desempenho acadêmico");
        assertThat(regraDeMedia.getGrupo()).isEqualTo("media_comparativa");
        assertThat(regraDeLivro.getDescricao()).isEqualTo("Pontos por empréstimos de livros");
        assertThat(regraDeLivro.getCategoria()).isEqualTo("Acervo e empréstimos");
        assertThat(regraDeLivro.getGrupo()).isNull();
    }

    @Test
    void removeMarcadorVisualDeGamificacaoEPreservaValoresVariaveis() {
        Regra turbo = Regra.builder()
                .descricao("[TURBO] 15 a 100 Pontos por Campanhas Educativas")
                .senso(Senso.builder().descricao("Saúde").build())
                .build();
        Regra variavel = Regra.builder()
                .descricao("Pontos por trapaça")
                .senso(Senso.builder().descricao("Utilização").build())
                .build();

        RegraCategorias.aplicar(List.of(turbo, variavel));

        assertThat(turbo.getDescricao()).isEqualTo("50 a 300 pontos por divulgação dos processos seletivos");
        assertThat(variavel.getDescricao()).contains("X pontos");
    }

    @Test
    void separaAtividadesExtracurricularesDeRegrasGerais() {
        Senso saude = Senso.builder().descricao("Saúde").build();
        List<Regra> regras = List.of(
                Regra.builder().descricao("2 pontos por aluno participante de olimpíada").senso(saude).build(),
                Regra.builder().descricao("1 ponto por aluno monitor no bimestre").senso(saude).build(),
                Regra.builder().descricao("1 ponto por aluno com plano de estudos").senso(saude).build(),
                Regra.builder().descricao("40 pontos por mais de 75% de responsáveis presentes na reunião")
                        .senso(saude).build(),
                Regra.builder().descricao("10 pontos para a turma com maior participação no Conselho de Classe")
                        .senso(saude).build());

        RegraCategorias.aplicar(regras);

        assertThat(regras).extracting(Regra::getCategoria).containsExactly(
                "Atividades extracurriculares",
                "Atividades extracurriculares",
                "Outros",
                "Outros",
                "Atividades extracurriculares");
    }

	@Test
	void substituiRegraLegadaDaExpotecPelaRegraCanonicaDeEventos() {
		Regra expotec = Regra.builder()
				.descricao("10 a 140 pontos por premiação da turma na Expotec/Semadec do campus")
				.senso(Senso.builder().descricao("Saúde").build())
				.build();

		RegraCategorias.aplicar(List.of(expotec));

		assertThat(expotec.getDescricao())
				.isEqualTo("10 a 140 pontos para as turmas com maior pontuação em eventos do campus");
		assertThat(expotec.getCategoria()).isEqualTo("Pesquisa, extensão e eventos");
	}

    @Test
    void mantemSuspensaoHistoricaDeUmPontoForaDosAliasesDaRegraAtiva() {
        Senso autodisciplina = Senso.builder().descricao("Autodisciplina").build();
        Regra historica = Regra.builder()
                .descricao(RegraCategorias.DESCRICAO_SUSPENSAO_HISTORICA_INATIVA)
                .senso(autodisciplina)
                .build();
        Regra canonica = Regra.builder()
                .descricao(RegraCategorias.DESCRICAO_SUSPENSAO_ATIVA)
                .senso(autodisciplina)
                .build();

        RegraCategorias.aplicar(List.of(canonica));

        assertThat(RegraCategorias.corresponde(historica, canonica)).isFalse();
    }
}
