package com.amorif.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.amorif.entities.Regra;

public record RegraAdminDtoResponse(
        Long id, String codigoVersao, int versao, String descricao, String grupo, String categoria,
        boolean ativo, String operacao, int valorMinimo, int valorMaximo,
        Long sensoId, String senso, Long tipoRegraId, String tipoRegra,
        boolean fixo, boolean temAluno, boolean automatico, boolean bimestreExtra,
        Integer frequencia, boolean porTurno, boolean temOlimpiada,
        List<Long> roleIds, List<String> roles, String criadoPor, String atualizadoPor,
        LocalDateTime criadoEm, LocalDateTime atualizadoEm) {

    public static RegraAdminDtoResponse from(Regra regra) {
        return new RegraAdminDtoResponse(
                regra.getId(), regra.getCodigoVersao(), regra.getVersao(), regra.getDescricao(), regra.getGrupo(),
                regra.getCategoria(), regra.isAtivo(), regra.getOperacao(), regra.getValorMinimo(), regra.getValorMaximo(),
                regra.getSenso().getId(), regra.getSenso().getDescricao(), regra.getTipoRegra().getId(),
                regra.getTipoRegra().getDescricao(), regra.getTipoRegra().isFixo(), regra.getTipoRegra().isTemAluno(),
                regra.getTipoRegra().isAutomatico(), regra.getTipoRegra().isBimestreExtra(),
                regra.getTipoRegra().getFrequencia(), regra.getTipoRegra().isPorTurno(),
                Boolean.TRUE.equals(regra.getTipoRegra().getTemOlimpiada()),
                regra.getRoles().stream().map(role -> role.getId()).toList(),
                regra.getRoles().stream().map(role -> role.getName()).toList(),
                regra.getCriadoPor(), regra.getAtualizadoPor(), regra.getCriadoEm(), regra.getAtualizadoEm());
    }
}
