package com.amorif.services.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amorif.dto.request.PontuacaoDtoRequest;
import com.amorif.dto.request.TurmaDtoRequest;
import com.amorif.dto.response.AnoLetivoDtoResponse;
import com.amorif.dto.response.PontuacaoDtoResponse;
import com.amorif.dto.response.TurmaDtoResponse;
import com.amorif.entities.AnoLetivo;
import com.amorif.entities.Pontuacao;
import com.amorif.entities.Turma;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.exceptions.ClosedSchoolYearException;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.repository.TurmaRepository;
import com.amorif.services.ManagerService;

/**
 * @author osailton
 */

@Service
public class ManagerServiceImpl implements ManagerService {

	private static final Logger logger = LoggerFactory.getLogger(ManagerServiceImpl.class);
	private final TurmaRepository turmaRepository;
	private final AnoLetivoRepository anoLetivoRepository;
	private final PontuacaoRepository pontuacaoRepository;

	public ManagerServiceImpl(TurmaRepository turmaRepository, AnoLetivoRepository anoLetivoRepository,
			PontuacaoRepository pontuacaoRepository) {
		this.turmaRepository = turmaRepository;
		this.anoLetivoRepository = anoLetivoRepository;
		this.pontuacaoRepository = pontuacaoRepository;
	}

	@Override
	public TurmaDtoResponse createTurma(TurmaDtoRequest request) {
		Optional<AnoLetivo> ano = Optional
				.ofNullable(this.anoLetivoRepository.getOpenAnoLetivoById(request.getIdAnoLetivo())
						.orElseThrow(() -> new InvalidArgumentException("O ano letivo não existe ou está encerrado!")));
		if (!ano.isEmpty()) {
			if (((AnoLetivo) ano.get()).getTurmas().stream().anyMatch(t -> t.getNome().contains(request.getNome()))) {
				throw new InvalidArgumentException(
						"Já existe uma turma cadastrada com esse nome para o ano letivo selecionado!");
			}
			Turma turma = Turma.builder().anoLetivo((AnoLetivo) ano.get()).nome(request.getNome())
					.descricao(request.getDescricao()).build();
			this.turmaRepository.save(turma);
			return turmaToTurmaDtoResponse(turma);
		} else {
			throw new InvalidArgumentException("Parâmetros inválidos para a requisição!");
		}
	}

	@Override
	public PontuacaoDtoResponse approvePoints(PontuacaoDtoRequest request) {
		Turma turma = this.turmaRepository.getReferenceById(request.getIdTurma());
		if (turma != null) {
			Pontuacao pontuacao = this.pontuacaoRepository.getByContadorTurma(request.getContador(), turma);
			if (pontuacao != null) {
				ensureSchoolYearIsOpen(pontuacao);
				pontuacao.setAplicado(true);
				pontuacao.setAnulado(false);
				pontuacao = this.pontuacaoRepository.save(pontuacao);

				logger.info("PONTUACAO APLICADA | REGRA: " + pontuacao.getRegra().getDescricao() + " | "
						+ pontuacao.getTurma().getNome() + " | CONTADOR: " + pontuacao.getContador());

				return this.dtoFromPontuacao(pontuacao);
			} else {
				throw new InvalidArgumentException("Parâmetros inválidos para a requisição!");
			}
		} else {
			throw new InvalidArgumentException("Parâmetros inválidos para a requisição!");
		}
	}

	@Override
	public PontuacaoDtoResponse cancelPoints(PontuacaoDtoRequest request) {
		Turma turma = this.turmaRepository.getReferenceById(request.getIdTurma());
		if (turma != null) {
			Pontuacao pontuacao = this.pontuacaoRepository.getByContadorTurma(request.getContador(), turma);
			if (pontuacao != null) {
				ensureSchoolYearIsOpen(pontuacao);
				pontuacao.setAnulado(true);
				pontuacao.setAplicado(false);
				pontuacao = this.pontuacaoRepository.save(pontuacao);

				logger.info("PONTUACAO ANULADA | REGRA: " + pontuacao.getRegra().getDescricao() + " | "
						+ pontuacao.getTurma().getNome() + " | CONTADOR: " + pontuacao.getContador());

				return this.dtoFromPontuacao(pontuacao);
			} else {
				throw new InvalidArgumentException("Parâmetros inválidos para a requisição!");
			}
		} else {
			throw new InvalidArgumentException("Parâmetros inválidos para a requisição!");
		}
	}

	@Override
	@Transactional
	public List<PontuacaoDtoResponse> approveAllPoints(List<PontuacaoDtoRequest> requests) {
		return requests.stream().map(this::approvePoints).toList();
	}

	private void ensureSchoolYearIsOpen(Pontuacao pontuacao) {
		if (pontuacao.getAnoLetivo() == null || !pontuacao.getAnoLetivo().isAberto()) {
			throw new ClosedSchoolYearException(
					"Não é possível alterar pontuações porque o ano letivo está fechado.");
		}
	}

	private TurmaDtoResponse turmaToTurmaDtoResponse(Turma turma) {
		return TurmaDtoResponse.builder()
				.anoLetivo(new AnoLetivoDtoResponse.Builder().id(turma.getAnoLetivo().getId())
						.anoLetivo(turma.getAnoLetivo().getAno()).build())
				.nome(turma.getNome()).descricao(turma.getDescricao()).pontuacao(0).build();
	}

	private PontuacaoDtoResponse dtoFromPontuacao(Pontuacao pontuacao) {
		return PontuacaoDtoResponse.builder().contador(pontuacao.getContador())
				.nomeTurma(pontuacao.getTurma().getNome()).idTurma(pontuacao.getTurma().getId())
				.descricao(pontuacao.getMotivacao()).pontos(pontuacao.getPontos())
				.operacao(pontuacao.getRegra().getOperacao().toString()).aplicado(pontuacao.isAplicado())
				.anulado(pontuacao.isAnulado()).build();
	}
}
