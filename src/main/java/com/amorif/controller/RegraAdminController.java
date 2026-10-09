package com.amorif.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amorif.dto.request.RegraAdminDtoRequest;
import com.amorif.dto.response.RegraAdminDtoResponse;
import com.amorif.services.RegraAdminService;

@RestController
@RequestMapping("/api/admin/regras")
public class RegraAdminController {
    private final RegraAdminService service;

    public RegraAdminController(RegraAdminService service) { this.service = service; }

    @GetMapping
    public List<RegraAdminDtoResponse> listar() { return service.listar(); }

    @GetMapping("/metadados")
    public Map<String, Object> metadados() { return service.metadados(); }

    @PostMapping
    public RegraAdminDtoResponse criar(@RequestBody RegraAdminDtoRequest request) { return service.criar(request); }

    @PutMapping("/{id}")
    public RegraAdminDtoResponse atualizar(@PathVariable Long id, @RequestBody RegraAdminDtoRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/status/{ativo}")
    public RegraAdminDtoResponse alterarStatus(@PathVariable Long id, @PathVariable boolean ativo) {
        return service.alterarStatus(id, ativo);
    }
}
