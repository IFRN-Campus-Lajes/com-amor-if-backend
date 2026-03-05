package com.amorif.entities;

import java.util.Arrays;
import java.util.List;

/**
 * @author osailton
 */

public enum RoleEnum {
	
	// Grouping with categories
	ROLE_ALUNO("General"),
    ROLE_AVAL("Aval"),
    ROLE_SISTEMA("Admin"),
    ROLE_ADMINISTRADOR("Admin"),
    ROLE_BIBLIOTECARIO("Aval"),
    ROLE_APOIO_ACADEMICO("Aval"),
    ROLE_ASSESSORIA_PEDAGOGICA("Aval"),
    ROLE_ASSISTENCIA_ESTUDANTIL("Aval"),
    ROLE_ASSESSORIA_LABORATORIO("Aval"),
    ROLE_COORDENADOR_CURSO("Aval"),
    ROLE_COEXPEIN("Aval"),
    ROLE_DIAD("Aval"),
    ROLE_DOCENTE("Aval"),
	ROLE_SEAC("Aval");

    private final String category;

    RoleEnum(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    // Method to filter roles by category
    public static List<RoleEnum> getByCategory(String category) {
        return Arrays.stream(values())
                     .filter(role -> role.category.equals(category))
                     .toList();
    }
  
}
