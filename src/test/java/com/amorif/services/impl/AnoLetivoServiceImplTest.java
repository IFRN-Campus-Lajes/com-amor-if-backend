package com.amorif.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amorif.dto.request.AnoLetivoDtoRequest;
import com.amorif.entities.AnoLetivo;
import com.amorif.repository.AnoLetivoRepository;

@ExtendWith(MockitoExtension.class)
class AnoLetivoServiceImplTest {

	@Mock
	private AnoLetivoRepository repository;

	private AnoLetivoServiceImpl service;
	private AnoLetivo ano2023;
	private AnoLetivo ano2024;

	@BeforeEach
	void setUp() {
		service = new AnoLetivoServiceImpl(repository);
		ano2023 = AnoLetivo.builder().id(1L).ano(2023).aberto(true).build();
		ano2024 = AnoLetivo.builder().id(2L).ano(2024).aberto(false).build();
	}

	@Test
	void openingOneSchoolYearShouldCloseThePreviouslyOpenYear() {
		AnoLetivoDtoRequest request = requestFor(ano2024, true);
		when(repository.getReferenceById(ano2024.getId())).thenReturn(ano2024);
		when(repository.findAll()).thenReturn(List.of(ano2023, ano2024));
		when(repository.save(ano2024)).thenReturn(ano2024);

		service.postAnoLetivo(request);

		assertFalse(ano2023.isAberto());
		assertTrue(ano2024.isAberto());
	}

	@Test
	void closingTheOnlyOpenSchoolYearShouldLeaveAllYearsClosed() {
		AnoLetivoDtoRequest request = requestFor(ano2023, false);
		when(repository.getReferenceById(ano2023.getId())).thenReturn(ano2023);
		when(repository.save(ano2023)).thenReturn(ano2023);

		service.postAnoLetivo(request);

		assertFalse(ano2023.isAberto());
		assertFalse(ano2024.isAberto());
		verify(repository, never()).findAll();
	}

	private AnoLetivoDtoRequest requestFor(AnoLetivo anoLetivo, boolean aberto) {
		AnoLetivoDtoRequest request = new AnoLetivoDtoRequest();
		request.setId(anoLetivo.getId());
		request.setAnoLetivo(anoLetivo.getAno());
		request.setAberto(aberto);
		return request;
	}
}
