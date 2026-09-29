package com.amorif.dto.response;

import java.io.Serializable;
import java.util.Date;

import com.amorif.entities.AnoLetivo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author osailton
 */
@Builder
@Getter
@Setter
public class PontuacaoDtoResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer contador;
	private TurmaDtoResponse turma;
	private String nomeTurma;
	private Long idTurma;
	private RegraDtoResponse regra;
	private String operacao;
	private int bimestre;
	private AnoLetivo anoLetivo;
	private Integer pontos;
	private String descricao;
	private boolean aplicado;
	private boolean anulado;
	private String matriculaAluno;
	private Date createdAt;
	private Long idUser;
	private UserDtoResponse criadoPor;
	private OlimpiadaDtoResponse olimpiada;

	public PontuacaoDtoResponse() {

	}

	public PontuacaoDtoResponse(Integer contador, TurmaDtoResponse turma, String nomeTurma, Long idTurma,
			RegraDtoResponse regra, String operacao, int bimestre, AnoLetivo anoLetivo, Integer pontos,
			String descricao, boolean aplicado, boolean anulado, String matriculaAluno, Date createdAt, Long idUser,
			UserDtoResponse criadoPor, OlimpiadaDtoResponse olimpiada) {
		super();
		this.contador = contador;
		this.nomeTurma = nomeTurma;
		this.idTurma = idTurma;

		if (turma != null) {
			this.turma = turma;
		} else {
			this.turma = TurmaDtoResponse.builder().nome(this.nomeTurma).id(this.idTurma).build();
		}

		this.regra = regra;
		this.operacao = operacao;
		this.bimestre = bimestre;
		this.anoLetivo = anoLetivo;
		this.pontos = pontos;
		this.descricao = descricao;
		this.aplicado = aplicado;
		this.anulado = anulado;
		this.matriculaAluno = matriculaAluno;
		this.createdAt = createdAt;
		this.idUser = idUser;
		this.criadoPor = criadoPor;
		this.olimpiada = olimpiada;
	}

}
