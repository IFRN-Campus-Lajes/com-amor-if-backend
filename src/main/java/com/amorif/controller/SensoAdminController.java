package com.amorif.controller;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amorif.dto.request.SensoDtoRequest;
import com.amorif.entities.Senso;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.exceptions.ReferencedSenseException;
import com.amorif.repository.CategoriaRegraRepository;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.SensoRepository;

@RestController
@RequestMapping("/api/admin/sensos")
public class SensoAdminController {
    private final SensoRepository sensoRepository;
    private final RegraRepository regraRepository;
    private final CategoriaRegraRepository categoriaRepository;

    public SensoAdminController(SensoRepository sensoRepository, RegraRepository regraRepository,
            CategoriaRegraRepository categoriaRepository) {
        this.sensoRepository = sensoRepository;
        this.regraRepository = regraRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Senso> listar() {
        return sensoRepository.findAllByOrderByDescricaoAsc();
    }

    @PostMapping
    public Senso criar(@RequestBody SensoDtoRequest request) {
        String descricao = validarDescricao(request.descricao(), null);
        return sensoRepository.save(Senso.builder().descricao(descricao).ativo(true).build());
    }

    @PutMapping("/{id}")
    @Transactional
    public Senso atualizar(@PathVariable Long id, @RequestBody SensoDtoRequest request) {
        Senso senso = buscar(id);
        senso.setDescricao(validarDescricao(request.descricao(), id));
        return sensoRepository.save(senso);
    }

    @PatchMapping("/{id}/status/{ativo}")
    @Transactional
    public Senso alterarStatus(@PathVariable Long id, @PathVariable boolean ativo) {
        Senso senso = buscar(id);
        if (!ativo && estaReferenciado(id)) {
            throw new ReferencedSenseException(
                    "Sensos referenciados por regras ou categorias devem permanecer ativos.");
        }
        senso.setAtivo(ativo);
        return sensoRepository.save(senso);
    }

    private Senso buscar(Long id) {
        return sensoRepository.findById(id)
                .orElseThrow(() -> new InvalidArgumentException("Senso não encontrado."));
    }

    private String validarDescricao(String descricao, Long idAtual) {
        String normalizada = descricao == null ? null : descricao.trim();
        if (normalizada == null || normalizada.isBlank()) {
            throw new InvalidArgumentException("Informe a descrição do senso.");
        }
        sensoRepository.findByDescricaoIgnoreCase(normalizada)
                .filter(senso -> !senso.getId().equals(idAtual))
                .ifPresent(senso -> {
                    throw new InvalidArgumentException("Já existe um senso com essa descrição.");
                });
        return normalizada;
    }

    private boolean estaReferenciado(Long id) {
        return regraRepository.existsBySenso_Id(id) || categoriaRepository.existsBySenso_Id(id);
    }
}
