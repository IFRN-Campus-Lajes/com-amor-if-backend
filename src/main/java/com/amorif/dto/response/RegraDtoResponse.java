package com.amorif.dto.response;

import java.io.Serializable;

import com.amorif.entities.Regra;
import com.amorif.entities.Senso;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegraDtoResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    private Long id;
    private String descricao;
    private String categoria;
    private Senso senso;

    // getters e setters

    public static RegraDtoResponse fromRegra(Regra regra) {
        return new RegraDtoResponse(regra.getId(), regra.getDescricao(), regra.getCategoria(), regra.getSenso());
    }

}
