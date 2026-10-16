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
import org.springframework.transaction.annotation.Transactional;

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
    public List<CategoriaRegra> listar() { return repository.findAllByOrderBySenso_DescricaoAscNomeAsc(); }

    @PostMapping
    public CategoriaRegra criar(@RequestBody CategoriaRegraDtoRequest request) {
        Senso senso = validarSenso(request.sensoId());
        String nome = validarNome(request.nome(), senso.getId(), null, null);
        return repository.save(CategoriaRegra.builder().nome(nome).senso(senso)
                .ativo(request.ativo() == null || request.ativo()).build());
    }

    @PutMapping("/{id}")
    @Transactional
    public CategoriaRegra atualizar(@PathVariable Long id,
            @RequestBody CategoriaRegraDtoRequest request) {
        CategoriaRegra categoria = repository.findById(id)
                .orElseThrow(() -> new InvalidArgumentException("Categoria não encontrada."));
        Long sensoId = categoria.getSenso().getId();
        Senso senso = validarSenso(request.sensoId());
        String nomeAnterior = categoria.getNome();
        String nome = validarNome(request.nome(), senso.getId(), nomeAnterior, sensoId);
        boolean usada = regraRepository.existsByCategoriaIgnoreCaseAndSenso_Id(nomeAnterior, sensoId);
        if (!sensoId.equals(senso.getId()) && usada)
            throw new InvalidArgumentException("O senso de uma categoria usada por regras não pode ser alterado.");
        if (Boolean.FALSE.equals(request.ativo())
                && regraRepository.existsByCategoriaIgnoreCaseAndSenso_IdAndAtivoTrue(nomeAnterior, sensoId))
            throw new InvalidArgumentException("Desative as regras ativas da categoria antes de inativá-la.");
        if (!nomeAnterior.equals(nome)) {
            regraRepository.findByCategoriaIgnoreCaseAndSenso_Id(nomeAnterior, sensoId)
                    .forEach(regra -> regra.setCategoria(nome));
            regraRepository.flush();
        }
        categoria.setNome(nome);
        categoria.setSenso(senso);
        if (request.ativo() != null) categoria.setAtivo(request.ativo());
        return repository.save(categoria);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        CategoriaRegra categoria = repository.findById(id)
                .orElseThrow(() -> new InvalidArgumentException("Categoria não encontrada."));
        Long sensoId = categoria.getSenso().getId();
        if (regraRepository.existsByCategoriaIgnoreCaseAndSenso_Id(categoria.getNome(), sensoId))
            throw new InvalidArgumentException("Categorias usadas por regras não podem ser excluídas; desative-a.");
        repository.delete(categoria);
    }

    private String validarNome(String nome, Long sensoId, String nomeAtual, Long sensoIdAtual) {
        String normalizado = limpar(nome);
        if (normalizado == null) throw new InvalidArgumentException("Informe o nome da categoria.");
        repository.findByNomeIgnoreCaseAndSenso_Id(normalizado, sensoId)
                .filter(c -> nomeAtual == null || !c.getNome().equals(nomeAtual) || !sensoId.equals(sensoIdAtual))
                .ifPresent(c -> {
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
