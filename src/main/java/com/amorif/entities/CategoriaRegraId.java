package com.amorif.entities;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRegraId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private Long senso;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CategoriaRegraId other)) return false;
        return Objects.equals(nome, other.nome) && Objects.equals(senso, other.senso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, senso);
    }
}
