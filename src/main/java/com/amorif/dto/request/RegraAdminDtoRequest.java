package com.amorif.dto.request;

import java.util.List;

public record RegraAdminDtoRequest(
        String descricao,
        String grupo,
        String categoria,
        String operacao,
        Integer valorMinimo,
        Integer valorMaximo,
        Long sensoId,
        Long tipoRegraId,
        List<Long> roleIds,
        Boolean ativo) {}
