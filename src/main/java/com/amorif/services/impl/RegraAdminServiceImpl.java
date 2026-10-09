package com.amorif.services.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amorif.dto.request.RegraAdminDtoRequest;
import com.amorif.dto.response.RegraAdminDtoResponse;
import com.amorif.entities.Regra;
import com.amorif.entities.Role;
import com.amorif.entities.Senso;
import com.amorif.entities.TipoRegra;
import com.amorif.entities.CategoriaRegra;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.exceptions.RuleNotFoundException;
import com.amorif.repository.CategoriaRegraRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.RoleRepository;
import com.amorif.repository.SensoRepository;
import com.amorif.repository.TipoRegraRepository;
import com.amorif.services.RegraAdminService;

@Service
public class RegraAdminServiceImpl implements RegraAdminService {
    private final RegraRepository regraRepository;
    private final PontuacaoRepository pontuacaoRepository;
    private final SensoRepository sensoRepository;
    private final TipoRegraRepository tipoRegraRepository;
    private final RoleRepository roleRepository;
    private final CategoriaRegraRepository categoriaRepository;

    public RegraAdminServiceImpl(RegraRepository regraRepository, PontuacaoRepository pontuacaoRepository,
            SensoRepository sensoRepository, TipoRegraRepository tipoRegraRepository,
            RoleRepository roleRepository, CategoriaRegraRepository categoriaRepository) {
        this.regraRepository = regraRepository;
        this.pontuacaoRepository = pontuacaoRepository;
        this.sensoRepository = sensoRepository;
        this.tipoRegraRepository = tipoRegraRepository;
        this.roleRepository = roleRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegraAdminDtoResponse> listar() {
        return regraRepository.findAllByOrderBySenso_DescricaoAscCategoriaAscDescricaoAsc().stream()
                .map(RegraAdminDtoResponse::from).toList();
    }

    @Override
    @Transactional
    public RegraAdminDtoResponse criar(RegraAdminDtoRequest request) {
        Regra regra = preencher(Regra.builder().codigoVersao(UUID.randomUUID().toString()).versao(1).build(), request);
        regra.setCriadoPor(usuarioAtual());
        regra.setAtualizadoPor(usuarioAtual());
        return RegraAdminDtoResponse.from(regraRepository.save(regra));
    }

    @Override
    @Transactional
    public RegraAdminDtoResponse atualizar(Long id, RegraAdminDtoRequest request) {
        Regra atual = regraRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Regra não encontrada."));
        Regra destino = atual;
        if (pontuacaoRepository.existsByRegra_Id(id)) {
            atual.setAtivo(false);
            atual.setAtualizadoPor(usuarioAtual());
            regraRepository.save(atual);
            destino = Regra.builder().codigoVersao(codigo(atual)).versao(Math.max(1, atual.getVersao()) + 1)
                    .criadoPor(usuarioAtual()).build();
        }
        preencher(destino, request);
        destino.setAtualizadoPor(usuarioAtual());
        return RegraAdminDtoResponse.from(regraRepository.save(destino));
    }

    @Override
    @Transactional
    public RegraAdminDtoResponse alterarStatus(Long id, boolean ativo) {
        Regra regra = regraRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException("Regra não encontrada."));
        regra.setAtivo(ativo);
        regra.setAtualizadoPor(usuarioAtual());
        return RegraAdminDtoResponse.from(regraRepository.save(regra));
    }

    @Override
    @Transactional
    public Map<String, Object> metadados() {
        regraRepository.findDistinctCategorias().forEach(nome -> {
            if (categoriaRepository.findByNomeIgnoreCase(nome).isEmpty()) {
                categoriaRepository.save(CategoriaRegra.builder().nome(nome).ativo(true).build());
            }
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sensos", sensoRepository.findAll());
        result.put("tipos", tipoRegraRepository.findAll());
        result.put("roles", roleRepository.findAll().stream()
                .filter(role -> !"ROLE_ALUNO".equals(role.getName())).toList());
        result.put("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return result;
    }

    private Regra preencher(Regra regra, RegraAdminDtoRequest request) {
        String descricao = textoObrigatorio(request.descricao(), "Informe a descrição da regra.");
        String operacao = textoObrigatorio(request.operacao(), "Informe a operação.").toUpperCase();
        if (!List.of("SUM", "SUB").contains(operacao))
            throw new InvalidArgumentException("A operação deve ser SUM ou SUB.");
        if (request.valorMinimo() == null || request.valorMinimo() < 0)
            throw new InvalidArgumentException("O valor mínimo deve ser maior ou igual a zero.");
        int maximo = request.valorMaximo() == null ? request.valorMinimo() : request.valorMaximo();
        if (maximo < request.valorMinimo())
            throw new InvalidArgumentException("O valor máximo não pode ser menor que o mínimo.");
        Senso senso = sensoRepository.findById(request.sensoId())
                .orElseThrow(() -> new InvalidArgumentException("Senso inválido."));
        TipoRegra tipo = tipoRegraRepository.findById(request.tipoRegraId())
                .orElseThrow(() -> new InvalidArgumentException("Tipo de regra inválido."));
        String categoria = textoObrigatorio(request.categoria(), "Informe a categoria.");
        categoriaRepository.findByNomeIgnoreCase(categoria)
                .filter(c -> c.isAtivo())
                .orElseThrow(() -> new InvalidArgumentException("Selecione uma categoria ativa cadastrada."));
        List<Role> roles = roleRepository.findAllById(request.roleIds() == null ? List.of() : request.roleIds());
        if (roles.isEmpty()) throw new InvalidArgumentException("Selecione ao menos um perfil autorizado.");
        if (tipo.isFixo() && maximo != request.valorMinimo()) maximo = request.valorMinimo();
        if (tipo.isAutomatico() && roles.stream().noneMatch(r -> "ROLE_SISTEMA".equals(r.getName())))
            throw new InvalidArgumentException("Regras automáticas precisam autorizar o perfil Sistema.");

        regra.setDescricao(descricao);
        regra.setGrupo(request.grupo() == null || request.grupo().isBlank() ? null : request.grupo().trim());
        regra.setCategoria(categoria);
        regra.setOperacao(operacao);
        regra.setValorMinimo(request.valorMinimo());
        regra.setValorMaximo(maximo);
        regra.setSenso(senso);
        regra.setTipoRegra(tipo);
        regra.setRoles(roles);
        regra.setAtivo(request.ativo() == null || request.ativo());
        return regra;
    }

    private String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new InvalidArgumentException(mensagem);
        return valor.trim();
    }

    private String usuarioAtual() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String codigo(Regra regra) {
        return regra.getCodigoVersao() == null || regra.getCodigoVersao().isBlank()
                ? UUID.randomUUID().toString() : regra.getCodigoVersao();
    }
}
