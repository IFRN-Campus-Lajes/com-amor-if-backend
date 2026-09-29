package com.amorif.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Entity
@Table(name = "olimpiada", uniqueConstraints = @UniqueConstraint(name = "uk_olimpiada_nome", columnNames = "nome"))
public class Olimpiada implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	public Olimpiada() {
	}

	public Olimpiada(Long id, String nome) {
		this.id = id;
		this.nome = nome;
	}
}
