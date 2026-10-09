package com.amorif.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
	@Builder.Default
	private boolean ativo = true;

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

	@Column(name = "codigo_versao")
	private String codigoVersao;

	@Column(nullable = false)
	@Builder.Default
	private int versao = 1;

	@Column(name = "criado_por")
	private String criadoPor;

	@Column(name = "atualizado_por")
	private String atualizadoPor;

	@Column(name = "criado_em", updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	@PrePersist
	void onCreate() {
		if (codigoVersao == null || codigoVersao.isBlank()) codigoVersao = UUID.randomUUID().toString();
		if (versao < 1) versao = 1;
		criadoEm = LocalDateTime.now();
		atualizadoEm = criadoEm;
	}

	@PreUpdate
	void onUpdate() {
		atualizadoEm = LocalDateTime.now();
	}

	public Regra(Long id, String descricao, String grupo, String categoria, boolean ativo, String operacao, int valorMinimo, int valorMaximo, Senso senso,
			TipoRegra tipoRegra, List<Role> roles, String codigoVersao, int versao, String criadoPor,
			String atualizadoPor, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
		super();
		this.id = id;
		this.descricao = descricao;
		this.grupo = grupo;
		this.categoria = categoria;
		this.ativo = ativo;
		this.operacao = operacao;
		this.valorMinimo = valorMinimo;
		this.valorMaximo = valorMaximo;
		this.senso = senso;
		this.tipoRegra = tipoRegra;
		this.roles = roles;
		this.codigoVersao = codigoVersao;
		this.versao = versao;
		this.criadoPor = criadoPor;
		this.atualizadoPor = atualizadoPor;
		this.criadoEm = criadoEm;
		this.atualizadoEm = atualizadoEm;
	}

	public Regra() {
		super();
	}
}
