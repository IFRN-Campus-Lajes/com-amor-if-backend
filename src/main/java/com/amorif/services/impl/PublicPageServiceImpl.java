package com.amorif.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.amorif.dto.response.AnoLetivoDtoResponse;
import com.amorif.dto.response.PontuacaoDtoResponse;
import com.amorif.dto.response.RegraDtoResponse;
import com.amorif.dto.response.TurmaDtoResponse;
import com.amorif.dto.response.UserDtoResponse;
import com.amorif.entities.AnoLetivo;
import com.amorif.entities.Pontuacao;
import com.amorif.entities.Turma;
import com.amorif.exceptions.TurmaNotFoundException;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.repository.TurmaRepository;
import com.amorif.services.PublicPageService;

/**
 * @author osailton
 */

@Service
public class PublicPageServiceImpl implements PublicPageService {

	private final PontuacaoRepository pontuacaoRepository;
	private final TurmaRepository turmaRepository;
	private final AnoLetivoRepository anoLetivoRepository;

	public PublicPageServiceImpl(PontuacaoRepository pontuacaoRepository, TurmaRepository turmaRepository,
			AnoLetivoRepository anoLetivoRepository) {
		this.pontuacaoRepository = pontuacaoRepository;
		this.turmaRepository = turmaRepository;
		this.anoLetivoRepository = anoLetivoRepository;
	}

	@Override
	public List<PontuacaoDtoResponse> listPontuacaoByTurma(Long turmaId) {
		Turma turma = this.turmaRepository.findById(turmaId)
				.orElseThrow(() -> new TurmaNotFoundException("Turma não encontrada"));

		List<Pontuacao> pontuacoes = this.pontuacaoRepository.findByTurma(turma);
		List<PontuacaoDtoResponse> response = new ArrayList<PontuacaoDtoResponse>();

		for (Pontuacao pontuacao : pontuacoes) {
			response.add(dtoFromPontuacao(pontuacao));
		}

		return response;
	}

	@Override
	public List<TurmaDtoResponse> listPontuacao() {
		// Instantiate the response list
		List<TurmaDtoResponse> listResponse = new ArrayList<>();

		// Get the last active AnoLetivo
		AnoLetivo anoLetivo = this.anoLetivoRepository.getLastActiveAnoLetivo();
		if (anoLetivo == null) {
			return listResponse;
		}

		// Get a list of Turma for that AnoLetivo
		List<Turma> turmas = this.turmaRepository.findAllByAnoLetivo(anoLetivo);

		// For each Turma get the sum of positive and negative points, creating TurmaDto
		for (Turma t : turmas) {
			int points = this.pontuacaoRepository.positivePoints(t) - this.pontuacaoRepository.negativePoints(t);
			TurmaDtoResponse dto = this.turmaToDto(t);
			dto.setPontuacao(points);
			listResponse.add(dto);
		}

		return listResponse;
	}

	private TurmaDtoResponse turmaToDto(Turma turma) {
		TurmaDtoResponse turmaDto = TurmaDtoResponse.builder().id(turma.getId())
				.anoLetivo(new AnoLetivoDtoResponse.Builder().id(turma.getAnoLetivo().getId())
						.anoLetivo(turma.getAnoLetivo().getAno()).build())
				.nome(turma.getNome()).descricao(turma.getDescricao()).build();
		return turmaDto;
	}

	private PontuacaoDtoResponse dtoFromPontuacao(Pontuacao pontuacao) {
		RegraDtoResponse regraDto = RegraDtoResponse.fromRegra(pontuacao.getRegra());

		return PontuacaoDtoResponse.builder().bimestre(pontuacao.getBimestre()).contador(pontuacao.getContador())
				.nomeTurma(pontuacao.getTurma().getNome()).idTurma(pontuacao.getTurma().getId())
				.descricao(pontuacao.getMotivacao()).pontos(pontuacao.getPontos())
				.operacao(pontuacao.getRegra().getOperacao()).aplicado(pontuacao.isAplicado())
				.createdAt(pontuacao.getData()).regra(regraDto).anulado(pontuacao.isAnulado())
				.matriculaAluno(pontuacao.getMatriculaAluno()).idUser(pontuacao.getUser().getId())
				.criadoPor(UserDtoResponse.builder().matricula(pontuacao.getUser().getMatricula())
						.email(pontuacao.getUser().getEmail()).username(pontuacao.getUser().getNome()).build())
				.build();
	}

}
