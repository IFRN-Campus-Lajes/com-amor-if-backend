package com.amorif.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.amorif.entities.Regra;

/** Atualiza o catálogo preservando os IDs referenciados pelas pontuações. */
public final class RegraCatalogoSincronizador {

    private RegraCatalogoSincronizador() {
    }

    public static List<Regra> sincronizar(List<Regra> existentes, List<Regra> canonicas) {
        List<Regra> regrasParaSalvar = new ArrayList<>();

        for (Regra canonica : canonicas) {
            Regra existente = existentes.stream()
                    .filter(item -> RegraCategorias.corresponde(item, canonica))
                    .findFirst()
                    .orElse(null);

            if (existente == null) {
                regrasParaSalvar.add(canonica);
            } else if (copiarConfiguracao(canonica, existente)) {
                regrasParaSalvar.add(existente);
            }
        }

        return regrasParaSalvar;
    }

    private static boolean copiarConfiguracao(Regra origem, Regra destino) {
        if (!Objects.equals(origem.getOperacao(), destino.getOperacao())) {
            throw new IllegalStateException("A operacao da regra existente nao pode ser alterada sem migrar as pontuacoes historicas: "
                    + destino.getDescricao());
        }

        boolean alterada = !Objects.equals(origem.getDescricao(), destino.getDescricao())
                || !Objects.equals(origem.getGrupo(), destino.getGrupo())
                || !Objects.equals(origem.getCategoria(), destino.getCategoria())
                || origem.getValorMinimo() != destino.getValorMinimo()
                || origem.getValorMaximo() != destino.getValorMaximo()
                || !Objects.equals(origem.getSenso(), destino.getSenso())
                || !Objects.equals(origem.getTipoRegra(), destino.getTipoRegra())
                || !mesmasRoles(origem, destino);

        if (!alterada) {
            return false;
        }

        destino.setDescricao(origem.getDescricao());
        destino.setGrupo(origem.getGrupo());
        destino.setCategoria(origem.getCategoria());
        destino.setValorMinimo(origem.getValorMinimo());
        destino.setValorMaximo(origem.getValorMaximo());
        destino.setSenso(origem.getSenso());
        destino.setTipoRegra(origem.getTipoRegra());
        destino.setRoles(origem.getRoles());
        return true;
    }

    private static boolean mesmasRoles(Regra primeira, Regra segunda) {
        List<String> primeiras = primeira.getRoles().stream().map(role -> role.getName()).sorted().toList();
        List<String> segundas = segunda.getRoles().stream().map(role -> role.getName()).sorted().toList();
        return primeiras.equals(segundas);
    }
}
