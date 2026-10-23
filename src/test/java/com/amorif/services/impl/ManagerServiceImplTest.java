package com.amorif.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amorif.dto.request.PontuacaoDtoRequest;
import com.amorif.entities.AnoLetivo;
import com.amorif.entities.Pontuacao;
import com.amorif.entities.Olimpiada;
import com.amorif.entities.Regra;
import com.amorif.entities.Turma;
import com.amorif.exceptions.ClosedSchoolYearException;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.repository.OlimpiadaRepository;
import com.amorif.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
class ManagerServiceImplTest {

	@Mock
	private TurmaRepository turmaRepository;
	@Mock
	private AnoLetivoRepository anoLetivoRepository;
	@Mock
	private PontuacaoRepository pontuacaoRepository;
	@Mock
	private OlimpiadaRepository olimpiadaRepository;

	private ManagerServiceImpl service;
	private PontuacaoDtoRequest request;
	private Pontuacao pontuacao;
	private Turma turma;

	@BeforeEach
	void setUp() {
		service = new ManagerServiceImpl(turmaRepository, anoLetivoRepository, pontuacaoRepository,
				olimpiadaRepository);
		turma = Turma.builder().id(1L).build();
		pontuacao = new Pontuacao();
		pontuacao.setAnoLetivo(AnoLetivo.builder().id(1L).ano(2023).aberto(false).build());
		request = new PontuacaoDtoRequest();
		request.setIdTurma(1L);
		request.setContador(1);
		when(turmaRepository.getReferenceById(1L)).thenReturn(turma);
		when(pontuacaoRepository.getByContadorTurma(1, turma)).thenReturn(pontuacao);
	}

	@Test
	void administratorShouldNotApprovePointsFromAClosedSchoolYear() {
		assertThrows(ClosedSchoolYearException.class, () -> service.approvePoints(request));

		verify(pontuacaoRepository, never()).save(pontuacao);
	}

	@Test
	void administratorShouldNotCancelPointsFromAClosedSchoolYear() {
		assertThrows(ClosedSchoolYearException.class, () -> service.cancelPoints(request));

		verify(pontuacaoRepository, never()).save(pontuacao);
	}

	@Test
	void shouldNotReactivateOlympiadParticipationAlreadyOccupiedByAnotherPoint() {
		AnoLetivo openYear = AnoLetivo.builder().id(1L).ano(2023).aberto(true).build();
		Turma turma = Turma.builder().id(1L).build();
		Olimpiada olimpiada = Olimpiada.builder().id(5L).nome("OBMEP").build();
		pontuacao.setAnoLetivo(openYear);
		pontuacao.setTurma(turma);
		pontuacao.setContador(1);
		pontuacao.setRegra(Regra.builder().id(7L).build());
		pontuacao.setOlimpiada(olimpiada);
		pontuacao.setMatriculaAluno("20151204010002");
		pontuacao.setAnulado(true);
		when(olimpiadaRepository.findByIdForUpdate(5L)).thenReturn(java.util.Optional.of(olimpiada));
		when(pontuacaoRepository.existsOtherActiveOlympiadParticipation(1L, 7L, 5L,
				"20151204010002", 1, 1L)).thenReturn(true);

		assertThrows(InvalidArgumentException.class, () -> service.approvePoints(request));
		verify(pontuacaoRepository, never()).save(pontuacao);
	}

	@Test
	void shouldApproveEveryPointFromACollectiveLaunch() {
		UUID grupoLancamentoId = UUID.randomUUID();
		AnoLetivo openYear = AnoLetivo.builder().id(1L).ano(2023).aberto(true).build();
		Turma segundaTurma = Turma.builder().id(2L).turno(0).build();
		Regra regra = Regra.builder().id(3L).operacao("SUB").build();
		turma.setTurno(0);
		pontuacao.setContador(1);
		pontuacao.setTurma(turma);
		pontuacao.setAnoLetivo(openYear);
		pontuacao.setBimestre(0);
		pontuacao.setRegra(regra);
		pontuacao.setGrupoLancamentoId(grupoLancamentoId);
		Pontuacao segunda = Pontuacao.builder().contador(1).turma(segundaTurma).anoLetivo(openYear).bimestre(0)
				.regra(regra).grupoLancamentoId(grupoLancamentoId).build();
		when(pontuacaoRepository.findAllByGrupoLancamentoId(grupoLancamentoId))
				.thenReturn(List.of(pontuacao, segunda));

		service.approvePoints(request);

		assertTrue(pontuacao.isAplicado());
		assertTrue(segunda.isAplicado());
		assertFalse(pontuacao.isAnulado());
		assertFalse(segunda.isAnulado());
		verify(pontuacaoRepository).saveAll(List.of(pontuacao, segunda));
	}

	@Test
	void shouldCancelEveryPointFromACollectiveLaunch() {
		UUID grupoLancamentoId = UUID.randomUUID();
		AnoLetivo openYear = AnoLetivo.builder().id(1L).ano(2023).aberto(true).build();
		Turma segundaTurma = Turma.builder().id(2L).turno(1).build();
		Regra regra = Regra.builder().id(3L).operacao("SUB").build();
		turma.setTurno(1);
		pontuacao.setContador(1);
		pontuacao.setTurma(turma);
		pontuacao.setAnoLetivo(openYear);
		pontuacao.setBimestre(0);
		pontuacao.setRegra(regra);
		pontuacao.setAplicado(true);
		pontuacao.setGrupoLancamentoId(grupoLancamentoId);
		Pontuacao segunda = Pontuacao.builder().contador(1).turma(segundaTurma).anoLetivo(openYear).bimestre(0)
				.regra(regra).aplicado(true).grupoLancamentoId(grupoLancamentoId).build();
		when(pontuacaoRepository.findAllByGrupoLancamentoId(grupoLancamentoId))
				.thenReturn(List.of(pontuacao, segunda));

		service.cancelPoints(request);

		assertFalse(pontuacao.isAplicado());
		assertFalse(segunda.isAplicado());
		assertTrue(pontuacao.isAnulado());
		assertTrue(segunda.isAnulado());
		verify(pontuacaoRepository).saveAll(List.of(pontuacao, segunda));
	}
}
