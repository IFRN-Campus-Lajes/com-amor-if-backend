package com.amorif.entities;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@Table(name = "tipo_regra")
public class TipoRegra implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private String descricao;
	
	@Column(nullable = false)
	private boolean fixo;

	@Column(nullable = false)
	private boolean temAluno;
	
	@Column(nullable = false)
	private boolean automatico;
	
	@Column(nullable = false)
	private boolean bimestreExtra;
	
	@Column(nullable = false)
	private Integer frequencia;
	
	@Column(nullable = false)
	@Builder.Default
	private boolean porTurno = false;

	@Column(nullable = true)
	@Builder.Default
	private Boolean temOlimpiada = false;
	
	@OneToMany(mappedBy = "tipoRegra", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
    private List<Regra> regras;
	
	public TipoRegra() {
		super();
	}

	public TipoRegra(Long id, String descricao, boolean fixo, boolean temAluno, boolean automatico,
			boolean bimestreExtra, Integer frequencia, boolean porTurno, Boolean temOlimpiada, List<Regra> regras) {
		super();
		this.id = id;
		this.descricao = descricao;
		this.fixo = fixo;
		this.temAluno = temAluno;
		this.automatico = automatico;
		this.bimestreExtra = bimestreExtra;
		this.frequencia = frequencia;
		this.porTurno = porTurno;
		this.temOlimpiada = temOlimpiada;
		this.regras = regras;
	}
	
}
