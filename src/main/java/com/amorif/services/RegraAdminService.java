package com.amorif.services;

import java.util.List;
import java.util.Map;

import com.amorif.dto.request.RegraAdminDtoRequest;
import com.amorif.dto.response.RegraAdminDtoResponse;

public interface RegraAdminService {
    List<RegraAdminDtoResponse> listar();
    RegraAdminDtoResponse criar(RegraAdminDtoRequest request);
    RegraAdminDtoResponse atualizar(Long id, RegraAdminDtoRequest request);
    RegraAdminDtoResponse alterarStatus(Long id, boolean ativo);
    Map<String, Object> metadados();
}
