package com.amorif.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.amorif.entities.ExecucaoAutomatica;
import com.amorif.repository.ExecucaoAutomaticaRepository;
import com.amorif.services.automaticas.ExecucaoAutomaticaService;

@RestController
@RequestMapping("/api/admin/regras-automaticas")
public class ExecucaoAutomaticaController {
    private final ExecucaoAutomaticaRepository repository;
    private final ExecucaoAutomaticaService service;
    public record Reprocessamento(Integer bimestre) {}

    public ExecucaoAutomaticaController(ExecucaoAutomaticaRepository repository, ExecucaoAutomaticaService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping("/execucoes")
    public Page<ExecucaoAutomatica> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return repository.findAll(PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)),
            Sort.by(Sort.Direction.DESC, "inicio", "id")));
    }

    @PostMapping("/reprocessar")
    public ExecucaoAutomatica reprocessar(@RequestBody Reprocessamento request, Authentication authentication) {
        return service.executar(request.bimestre(), "ADMINISTRATIVA", authentication.getName()).execucao();
    }
}
