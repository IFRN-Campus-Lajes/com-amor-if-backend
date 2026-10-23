package com.amorif.dto.request;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TurmaDtoRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	private Long id;

	@JsonProperty("ano_letivo_id")
	private Long idAnoLetivo;

	@JsonProperty("nome")
	private String nome;

	@JsonProperty("descricao")
	private String descricao;

	@JsonProperty("turno")
	private Integer turno;

	public TurmaDtoRequest() {

	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdAnoLetivo() {
		return idAnoLetivo;
	}

	public void setIdAnoLetivo(Long idAnoLetivo) {
		this.idAnoLetivo = idAnoLetivo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getTurno() {
		return turno;
	}

	public void setTurno(Integer turno) {
		this.turno = turno;
	}

	@Override
	public int hashCode() {
		return Objects.hash(descricao, id, idAnoLetivo, nome, turno);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TurmaDtoRequest other = (TurmaDtoRequest) obj;
		return Objects.equals(descricao, other.descricao) && Objects.equals(id, other.id)
				&& Objects.equals(idAnoLetivo, other.idAnoLetivo) && Objects.equals(nome, other.nome)
				&& Objects.equals(turno, other.turno);
	}
}
