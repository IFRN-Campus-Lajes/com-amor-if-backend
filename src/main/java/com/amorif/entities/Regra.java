package com.amorif.entities;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@Table(name = "regra")
public class Regra implements Serializable {
private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private String descricao;
	
	@Column(nullable = true)
	private String grupo;

	@Column(nullable = true)
	private String categoria;

	@Column(nullable = false)
	private String operacao;
	
	@Column(nullable = false)
	private int valorMinimo;
	
	@Column(nullable = false)
	@Builder.Default
	private int valorMaximo = 500;
	
	@ManyToOne
	@JoinColumn(name = "senso_id")
	private Senso senso;
	
	@ManyToOne
	@JoinColumn(name = "tipoRegra_id")
	private TipoRegra tipoRegra;
	
	@ManyToMany
    @JoinTable(
        name = "regra_funcao",
        joinColumns = @JoinColumn(name = "regra_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

	public Regra(Long id, String descricao, String grupo, String categoria, String operacao, int valorMinimo, int valorMaximo, Senso senso,
			TipoRegra tipoRegra, List<Role> roles) {
		super();
		this.id = id;
		this.descricao = descricao;
		this.grupo = grupo;
		this.categoria = categoria;
		this.operacao = operacao;
		this.valorMinimo = valorMinimo;
		this.valorMaximo = valorMaximo;
		this.senso = senso;
		this.tipoRegra = tipoRegra;
		this.roles = roles;
	}

	public Regra() {
		super();
	}
}
