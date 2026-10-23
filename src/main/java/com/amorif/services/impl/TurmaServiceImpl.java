package com.amorif.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.amorif.dto.request.TurmaDtoRequest;
import com.amorif.dto.response.AnoLetivoDtoResponse;
import com.amorif.dto.response.TurmaDtoResponse;
import com.amorif.entities.AnoLetivo;
import com.amorif.entities.Turma;
import com.amorif.entities.TurnoEnum;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.exceptions.InvalidTurnException;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.TurmaRepository;
import com.amorif.services.TurmaService;

/**
 * @author osailton
 */

@Service
public class TurmaServiceImpl implements TurmaService {

	private TurmaRepository turmaRepository;
	private AnoLetivoRepository anoLetivoRepository;

	public TurmaServiceImpl(TurmaRepository turmaRepository, AnoLetivoRepository anoLetivoRepository) {
		this.turmaRepository = turmaRepository;
		this.anoLetivoRepository = anoLetivoRepository;
	}

	@Override
	public List<TurmaDtoResponse> listByLastActiveAnoLetivo() {
		AnoLetivo ultimoAnoLetivo = anoLetivoRepository.getLastActiveAnoLetivo();
		if (ultimoAnoLetivo == null) {
			throw new InvalidArgumentException("Ano letivo não existe!");
		}
		return turmaRepository.findAllByAnoLetivo(ultimoAnoLetivo).stream().map(e -> dtoFromTurma(e))
				.collect(Collectors.toList());
	}

	@Override
	public List<TurmaDtoResponse> listAll() {
		return turmaRepository.findAll().stream().map(e -> dtoFromTurma(e)).collect(Collectors.toList());
	}

	@Override
	public List<TurmaDtoResponse> listByAno(Long idAno) {
		AnoLetivo ano = this.anoLetivoRepository.getReferenceById(idAno);
		if (ano != null) {
			return turmaRepository.findAllByAnoLetivo(ano).stream().map(e -> dtoFromTurma(e))
					.collect(Collectors.toList());
		} else {
			throw new InvalidArgumentException("Ano letivo não existe!");
		}
	}

	@Override
	public TurmaDtoResponse postTurma(TurmaDtoRequest turmaRequest) {
		Integer turno = requireValidTurn(turmaRequest.getTurno());
		if (turmaRequest.getId() != null) {

			Turma turma = this.turmaRepository.getReferenceById(turmaRequest.getId());
			if (turma != null) {

				turma.setNome(turmaRequest.getNome());
				turma.setDescricao(turmaRequest.getDescricao());
				turma.setTurno(turno);

				AnoLetivo ano = anoLetivoRepository.getReferenceById(turmaRequest.getIdAnoLetivo());
				if (ano != null) {
					turma.setAnoLetivo(ano);
				}

				turma = this.turmaRepository.save(turma);
				return this.dtoFromTurma(turma);
			} else {
				throw new InvalidArgumentException("Ano letivo não existe!");
			}
		} else {
			Turma turma = this.turmaFromDto(turmaRequest);
			turma = this.turmaRepository.save(turma);
			return this.dtoFromTurma(turma);
		}
	}

	private TurmaDtoResponse dtoFromTurma(Turma turma) {
		return TurmaDtoResponse.builder().id(turma.getId())
				.anoLetivo(new AnoLetivoDtoResponse.Builder().id(turma.getAnoLetivo().getId())
						.anoLetivo(turma.getAnoLetivo().getAno()).build())
				.nome(turma.getNome()).descricao(turma.getDescricao()).turno(turma.getTurno()).pontuacao(0).build();
	}

	private Turma turmaFromDto(TurmaDtoRequest dto) {
		return Turma.builder().nome(dto.getNome()).descricao(dto.getDescricao()).turno(dto.getTurno())
				.anoLetivo(this.anoLetivoRepository.getReferenceById(dto.getIdAnoLetivo())).build();
	}

	private Integer requireValidTurn(Integer turno) {
		if (turno == null || turno < 0 || turno >= TurnoEnum.values().length) {
			throw new InvalidTurnException("Selecione um turno válido para a turma.");
		}
		return turno;
	}

}
