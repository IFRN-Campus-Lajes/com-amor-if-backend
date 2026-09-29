package com.amorif.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author osailton
 * 
 *         Entity responsible to keep all point registers Each Turma has its own
 *         points
 */

@Getter
@Setter
@Builder
@Entity
@IdClass(PontuacaoID.class)
@Table(name = "pontuacao")
public class Pontuacao implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private Integer contador;

	@Id
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "turma_id")
	private Turma turma;
	
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "regra_id")
	private Regra regra;
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "ano_id")
	private AnoLetivo anoLetivo;
	
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "created_by")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "olimpiada_id", nullable = true)
	private Olimpiada olimpiada;

	private Integer bimestre;
	
	private Integer pontos;

	private String motivacao;
	
	private String matriculaAluno;

	private boolean aplicado;

	private boolean anulado;

	@Temporal(TemporalType.TIMESTAMP)
	private Date data;

	@Override
	public int hashCode() {
		return Objects.hash(contador, turma);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pontuacao other = (Pontuacao) obj;
		return Objects.equals(contador, other.contador) && Objects.equals(turma, other.turma);
	}
	
	public Pontuacao() {
		super();
	}

	public Pontuacao(Integer contador, Turma turma, Regra regra, AnoLetivo anoLetivo, User user, Olimpiada olimpiada, int bimestre,
			Integer pontos, String motivacao, String matriculaAluno, boolean aplicado, boolean anulado, Date data) {
		super();
		this.contador = contador;
		this.turma = turma;
		this.regra = regra;
		this.anoLetivo = anoLetivo;
		this.user = user;
		this.olimpiada = olimpiada;
		this.bimestre = bimestre;
		this.pontos = pontos;
		this.motivacao = motivacao;
		this.matriculaAluno = matriculaAluno;
		this.aplicado = aplicado;
		this.anulado = anulado;
		this.data = data;
	}
	
}
