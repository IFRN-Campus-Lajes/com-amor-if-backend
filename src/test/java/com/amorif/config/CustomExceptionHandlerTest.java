package com.amorif.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.amorif.dto.response.ErrorMessageDtoResponse;
import com.amorif.exceptions.ClosedSchoolYearException;

class CustomExceptionHandlerTest {

	@Test
	void closedSchoolYearShouldReturnConflict() {
		CustomExceptionHandler handler = new CustomExceptionHandler();
		String message = "Não é possível alterar pontuações porque não existe ano letivo aberto.";

		ResponseEntity<Object> response = handler.handleClosedSchoolYearException(
				new ClosedSchoolYearException(message), null);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		ErrorMessageDtoResponse body = (ErrorMessageDtoResponse) response.getBody();
		assertEquals(HttpStatus.CONFLICT, body.getStatus());
		assertEquals(List.of(message), body.getErrors());
	}
}
