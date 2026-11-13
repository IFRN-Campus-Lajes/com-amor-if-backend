package com.amorif.services.automaticas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amorif.entities.AnoLetivo;
import com.amorif.entities.FrequenciaRegraEnum;
import com.amorif.entities.Pontuacao;
import com.amorif.entities.Regra;
import com.amorif.entities.Role;
import com.amorif.entities.Turma;
import com.amorif.entities.User;
import com.amorif.exceptions.ClosedSchoolYearException;
import com.amorif.exceptions.InvalidBimesterException;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.RoleRepository;
import com.amorif.repository.TurmaRepository;
import com.amorif.repository.UserRepository;

@Service
public class PontuacaoAutomaticaService {
    private static final Logger log = LoggerFactory.getLogger(PontuacaoAutomaticaService.class);
    public static final String MATRICULA_SISTEMA = "!sistema-regras-automaticas";
    private final AnoLetivoRepository anos;
    private final TurmaRepository turmas;
    private final RegraRepository regras;
    private final PontuacaoRepository pontos;
    private final UserRepository usuarios;
    private final RoleRepository roles;
    private final AuditoriaAutomaticaService auditoria;
    public record Apuracao(List<Pontuacao> pontuacoes, int existentes, int inelegiveis) {}

    public PontuacaoAutomaticaService(AnoLetivoRepository anos, TurmaRepository turmas,
            RegraRepository regras, PontuacaoRepository pontos, UserRepository usuarios, RoleRepository roles, AuditoriaAutomaticaService auditoria) {
        this.auditoria = auditoria;
        this.anos = anos;
        this.turmas = turmas;
        this.regras = regras;
        this.pontos = pontos;
        this.usuarios = usuarios;
        this.roles = roles;
    }

    /** null processa os quatro bimestres comuns. A trava do ano serializa todos os disparos. */
    @Transactional
    public List<Pontuacao> processar(Integer bimestre) {
        return apurar(bimestre, null).pontuacoes();
    }

    @Transactional
    public Apuracao apurar(Integer bimestre, UUID execucaoId) {
        if (bimestre != null && (bimestre < 0 || bimestre > 3))
            throw new InvalidBimesterException("O processamento automático aceita apenas os quatro bimestres comuns.");
        AnoLetivo ano = anos.getLastActiveAnoLetivoForUpdate();
        if (ano == null) throw new ClosedSchoolYearException("Não existe ano letivo aberto.");
        String execucao = execucaoId == null ? UUID.randomUUID().toString() : execucaoId.toString();
        if (execucaoId != null) auditoria.registrarAno(execucaoId, ano.getId(), ano.getAno());
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    log.info("BONUS final | execucao={} ano={} resultado={}", execucao, ano.getId(),
                        status == STATUS_COMMITTED ? "confirmado" : "revertido");
                }
            });
        log.info("BONUS inicio | execucao={} ano={} bimestre={}", execucao, ano.getId(), bimestre);
        List<Regra> catalogo = regras.findAll();
        List<Regra> automaticas = catalogo.stream()
            .filter(r -> r.isAtivo() && r.getSenso().isAtivo() && r.getTipoRegra().isAutomatico())
            .sorted(Comparator.comparingInt(Regra::getVersao).thenComparing(Regra::getId).reversed()).toList();
        // Não interpretar novas condições silenciosamente: novos algoritmos exigem implementação explícita.
        for (Regra regra : automaticas) {
            if (BonusBimestral.DEFINICOES.stream().noneMatch(d -> d.reconhece(regra)))
                throw new IllegalStateException("Regra automática sem condições implementadas: " + regra.getId());
            if (!regra.getTipoRegra().isFixo() || regra.getTipoRegra().isBimestreExtra()
                    || regra.getTipoRegra().getFrequencia() != FrequenciaRegraEnum.BIMESTRAL.ordinal()
                    || !"SUM".equals(regra.getOperacao()))
                throw new IllegalStateException("Configuração incompatível com bônus bimestral: " + regra.getId());
        }
        List<Pontuacao> criadas = new ArrayList<>();
        List<Integer> periodos = bimestre == null ? IntStream.range(0, 4).boxed().toList() : List.of(bimestre);
        int existentes = 0;
        int inelegiveis = 0;
        User sistema = automaticas.isEmpty() ? null : usuarioSistema();
        for (Turma referencia : turmas.findAllByAnoLetivo(ano).stream().sorted(Comparator.comparing(Turma::getId)).toList()) {
            // Também usada pelo lançamento manual, protegendo o contador composto por turma.
            Turma turma = turmas.findByIdForUpdate(referencia.getId()).orElseThrow();
            List<Pontuacao> historico = new ArrayList<>(pontos.findByTurma(turma));
            for (Regra bonus : automaticas) {
                BonusBimestral condicao = BonusBimestral.DEFINICOES.stream()
                    .filter(d -> d.reconhece(bonus)).findFirst().orElseThrow();
                condicao.validarCatalogo(catalogo);
                for (Integer periodo : periodos) {
                    List<Pontuacao> doPeriodo = historico.stream()
                        .filter(p -> periodo.equals(p.getBimestre())).toList();
                    // Inclui anulações e versões históricas: só o administrador pode reativar.
                    if (doPeriodo.stream().anyMatch(p -> condicao.mesmaFamilia(p.getRegra(), bonus))) {
                        existentes++;
                        continue;
                    }
                    if (!condicao.elegivel(doPeriodo, catalogo)) { inelegiveis++; continue; }
                    int contador = historico.stream().mapToInt(Pontuacao::getContador).max().orElse(0) + 1;
                    Pontuacao nova = Pontuacao.builder().contador(contador).turma(turma).anoLetivo(ano)
                        .regra(bonus).user(sistema).bimestre(periodo).pontos(bonus.getValorMinimo())
                        .motivacao(bonus.getSenso().getDescricao() + " " + bonus.getDescricao())
                        .data(new Date()).aplicado(false).anulado(false).build();
                    pontos.save(nova);
                    historico.add(nova);
                    criadas.add(nova);
                    log.info("BONUS criado | execucao={} ano={} bimestre={} turma={} regra={} contador={}",
                        execucao, ano.getId(), periodo, turma.getId(), bonus.getId(), contador);
                }
            }
        }
        pontos.flush();
        log.info("BONUS calculado | execucao={} criados={} existentes={} inelegiveis={} (aguardando commit)",
            execucao, criadas.size(), existentes, inelegiveis);
        return new Apuracao(criadas, existentes, inelegiveis);
    }

    private User usuarioSistema() {
        return usuarios.findByMatricula(MATRICULA_SISTEMA).orElseGet(() -> {
            Role sistema = roles.getByName("ROLE_SISTEMA");
            if (sistema == null) throw new IllegalStateException("Perfil ROLE_SISTEMA não encontrado.");
            User usuario = new User();
            usuario.setMatricula(MATRICULA_SISTEMA);
            usuario.setNome("Sistema — regras automáticas");
            usuario.getFuncoes().addAll(Set.of(sistema));
            return usuarios.save(usuario);
        });
    }
}
