package com.amorif.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amorif.dto.request.TurmaDtoRequest;
import com.amorif.dto.response.TurmaDtoResponse;
import com.amorif.entities.AnoLetivo;
import com.amorif.entities.Turma;
import com.amorif.entities.TurnoEnum;
import com.amorif.exceptions.InvalidTurnException;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
class TurmaServiceImplTest {

	@Mock
	private TurmaRepository turmaRepository;
	@Mock
	private AnoLetivoRepository anoLetivoRepository;

	private TurmaServiceImpl service;
	private TurmaDtoRequest request;

	@BeforeEach
	void setUp() {
		service = new TurmaServiceImpl(turmaRepository, anoLetivoRepository);
		request = new TurmaDtoRequest();
		request.setNome("1º A");
		request.setDescricao("Informática");
		request.setIdAnoLetivo(1L);
	}

	@Test
	void shouldPersistAndReturnSchoolClassTurn() {
		request.setTurno(TurnoEnum.MATUTINO.ordinal());
		when(anoLetivoRepository.getReferenceById(1L))
				.thenReturn(AnoLetivo.builder().id(1L).ano(2026).aberto(true).build());
		when(turmaRepository.save(any(Turma.class))).thenAnswer(invocation -> {
			Turma turma = invocation.getArgument(0);
			turma.setId(10L);
			return turma;
		});

		TurmaDtoResponse response = service.postTurma(request);

		ArgumentCaptor<Turma> captor = ArgumentCaptor.forClass(Turma.class);
		verify(turmaRepository).save(captor.capture());
		assertEquals(TurnoEnum.MATUTINO.ordinal(), captor.getValue().getTurno());
		assertEquals(TurnoEnum.MATUTINO.ordinal(), response.getTurno());
	}

	@Test
	void shouldRejectSchoolClassWithoutTurn() {
		assertThrows(InvalidTurnException.class, () -> service.postTurma(request));
		verify(turmaRepository, never()).save(any(Turma.class));
	}
}
