package com.amorif.services.automaticas;

import java.util.List;
import java.util.Objects;

import com.amorif.config.RegraCategorias;
import com.amorif.entities.Pontuacao;
import com.amorif.entities.Regra;
import com.amorif.entities.Senso;

/** Condições em código, separadas do agendamento e da gravação de pontos. */
public record BonusBimestral(String senso, String excelente, List<String> impeditivas) {
    public static final List<BonusBimestral> DEFINICOES = List.of(
        new BonusBimestral("Ordenação", "10 pontos por organização excelente", List.of(
            "0 pontos por organização ruim", "5 pontos por organização mediana",
            "8 pontos por organização boa", "Perda de 10 a 50 pontos por desordem ou dano intencional")),
        new BonusBimestral("Limpeza", "10 pontos por limpeza excelente", List.of(
            "0 pontos por limpeza ruim", "5 pontos por limpeza mediana", "8 pontos por limpeza boa",
            "Perda de 10 pontos por falta de limpeza nas turmas do turno")));

    private Regra referencia(String descricao) {
        return Regra.builder().descricao(descricao).senso(Senso.builder().descricao(senso).build()).build();
    }

    public boolean reconhece(Regra regra) {
        return RegraCategorias.corresponde(regra,
                referencia("20 pontos por 100% de avaliações excelentes no bimestre"));
    }

    public boolean mesmaFamilia(Regra a, Regra b) {
        return Objects.equals(a.getId(), b.getId())
            || (a.getCodigoVersao() != null && !a.getCodigoVersao().isBlank()
                && a.getCodigoVersao().equals(b.getCodigoVersao()))
            || (reconhece(a) && reconhece(b));
    }

    private boolean corresponde(Regra regra, String descricao, List<Regra> catalogo) {
        Regra referencia = referencia(descricao);
        return catalogo.stream().filter(r -> RegraCategorias.corresponde(r, referencia))
            .anyMatch(r -> Objects.equals(r.getId(), regra.getId())
                || (r.getCodigoVersao() != null && !r.getCodigoVersao().isBlank()
                    && r.getCodigoVersao().equals(regra.getCodigoVersao())));
    }

    public void validarCatalogo(List<Regra> catalogo) {
        java.util.stream.Stream.concat(java.util.stream.Stream.of(excelente), impeditivas.stream())
            .forEach(descricao -> {
                if (catalogo.stream().noneMatch(r -> RegraCategorias.corresponde(r, referencia(descricao))))
                    throw new IllegalStateException("Condição do bônus não encontrada: " + senso + " | " + descricao);
            });
    }

    public boolean elegivel(List<Pontuacao> avaliacoes, List<Regra> catalogo) {
        // Avaliações pendentes e anuladas não determinam a concessão.
        List<Pontuacao> validas = avaliacoes.stream().filter(p -> p.isAplicado() && !p.isAnulado()).toList();
        return validas.stream().anyMatch(p -> corresponde(p.getRegra(), excelente, catalogo))
            && validas.stream().noneMatch(p -> impeditivas.stream()
                .anyMatch(descricao -> corresponde(p.getRegra(), descricao, catalogo)));
    }
}
