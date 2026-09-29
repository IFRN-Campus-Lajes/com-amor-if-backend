package com.amorif.dto.response;

import com.amorif.entities.Olimpiada;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OlimpiadaDtoResponse {
	private Long id;
	private String nome;

	public static OlimpiadaDtoResponse fromOlimpiada(Olimpiada olimpiada) {
		if (olimpiada == null) {
			return null;
		}
		return OlimpiadaDtoResponse.builder().id(olimpiada.getId()).nome(olimpiada.getNome()).build();
	}
}
