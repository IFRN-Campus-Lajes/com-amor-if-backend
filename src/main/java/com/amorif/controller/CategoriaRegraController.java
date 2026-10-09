package com.amorif.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amorif.dto.request.CategoriaRegraDtoRequest;
import com.amorif.entities.CategoriaRegra;
import com.amorif.entities.Senso;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.repository.CategoriaRegraRepository;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.SensoRepository;

@RestController
@RequestMapping("/api/admin/categorias-regras")
public class CategoriaRegraController {
    private final CategoriaRegraRepository repository;
    private final RegraRepository regraRepository;
    private final SensoRepository sensoRepository;

    public CategoriaRegraController(CategoriaRegraRepository repository, RegraRepository regraRepository,
            SensoRepository sensoRepository) {
        this.repository = repository;
        this.regraRepository = regraRepository;
        this.sensoRepository = sensoRepository;
    }

    @GetMapping
    public List<CategoriaRegra> listar() { return repository.findAllByOrderByNomeAsc(); }

    @PostMapping
    public CategoriaRegra criar(@RequestBody CategoriaRegraDtoRequest request) {
        String nome = validarNome(request.nome(), null);
        Senso senso = validarSenso(request.sensoId());
        return repository.save(CategoriaRegra.builder().nome(nome).senso(senso)
                .ativo(request.ativo() == null || request.ativo()).build());
    }

    @PutMapping("/{id}")
    public CategoriaRegra atualizar(@PathVariable Long id, @RequestBody CategoriaRegraDtoRequest request) {
        CategoriaRegra categoria = repository.findById(id)
                .orElseThrow(() -> new InvalidArgumentException("Categoria não encontrada."));
        String nomeAnterior = categoria.getNome();
        String nome = validarNome(request.nome(), id);
        Senso senso = validarSenso(request.sensoId());
        if (!nomeAnterior.equalsIgnoreCase(nome) && regraRepository.existsByCategoriaIgnoreCase(nomeAnterior))
            throw new InvalidArgumentException("A categoria já é usada por regras e não pode ser renomeada.");
        if (categoria.getSenso() != null && !categoria.getSenso().getId().equals(senso.getId())
                && regraRepository.existsByCategoriaIgnoreCase(nomeAnterior))
            throw new InvalidArgumentException("O senso de uma categoria usada por regras não pode ser alterado.");
        categoria.setNome(nome);
        categoria.setSenso(senso);
        if (request.ativo() != null) categoria.setAtivo(request.ativo());
        return repository.save(categoria);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        CategoriaRegra categoria = repository.findById(id)
                .orElseThrow(() -> new InvalidArgumentException("Categoria não encontrada."));
        if (regraRepository.existsByCategoriaIgnoreCase(categoria.getNome()))
            throw new InvalidArgumentException("Categorias usadas por regras não podem ser excluídas; desative-a.");
        repository.delete(categoria);
    }

    private String validarNome(String nome, Long idAtual) {
        String normalizado = limpar(nome);
        if (normalizado == null) throw new InvalidArgumentException("Informe o nome da categoria.");
        repository.findByNomeIgnoreCase(normalizado).filter(c -> !c.getId().equals(idAtual)).ifPresent(c -> {
            throw new InvalidArgumentException("Já existe uma categoria com esse nome.");
        });
        return normalizado;
    }

    private Senso validarSenso(Long sensoId) {
        if (sensoId == null) throw new InvalidArgumentException("Selecione o senso da categoria.");
        return sensoRepository.findById(sensoId)
                .orElseThrow(() -> new InvalidArgumentException("Senso inválido."));
    }

    private String limpar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
