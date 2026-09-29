package com.amorif.dto.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author osailton
 */

@Builder
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PontuacaoDtoRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("contador")
	private Integer contador;
	
	@JsonProperty("id_turma")
	private Long idTurma;
	
	@JsonProperty("id_regra")
	private Long idRegra;
	
	@JsonProperty("pontos")
	private Integer pontos;
	
	@JsonProperty("operacao")
	private String operacao;
	
	@JsonProperty("motivacao")
	private String motivacao;
	
	@JsonProperty("matricula_aluno")
	private String matriculaAluno;
	
	@JsonProperty("bimestre")
	private Integer bimestre;
	
	@JsonProperty("turno")
	private Integer turno;
	
	@JsonProperty("id_user")
	private Long idUser;

	@JsonProperty("id_olimpiada")
	private Long idOlimpiada;
	
	public PontuacaoDtoRequest() {

	}

	public PontuacaoDtoRequest(Integer contador, Long idTurma, Long idRegra, Integer pontos, String operacao,
			String motivacao, String matriculaAluno, Integer bimestre, Integer turno, Long idUser, Long idOlimpiada) {
		super();
		this.contador = contador;
		this.idTurma = idTurma;
		this.idRegra = idRegra;
		this.pontos = pontos;
		this.operacao = operacao;
		this.motivacao = motivacao;
		this.matriculaAluno = matriculaAluno;
		this.bimestre = bimestre;
		this.turno = turno;
		this.idUser = idUser;
		this.idOlimpiada = idOlimpiada;
	}
	
}
