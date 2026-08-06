package com.amorif.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SensoTest {

    private Senso senso;
    private List<Regra> regras;

    @BeforeEach
    void setUp() {
        // Inicializa um objeto de regra vazio para associar ao Senso
        regras = new ArrayList<>();
        Regra regra1 = Regra.builder().descricao("Regra de Teste 1").build();
        regras.add(regra1);

        // Cria um objeto Senso com dados iniciais
        senso = Senso.builder()
                .id(1L)
                .descricao("Senso de Teste")
                .regras(regras)
                .build();
    }

    @Test
    void testSensoAttributes() {
        // Verifica se os atributos foram atribuídos corretamente
        assertThat(senso.getId()).isEqualTo(1L);
        assertThat(senso.getDescricao()).isEqualTo("Senso de Teste");
        assertThat(senso.getRegras()).isNotNull();
        assertThat(senso.getRegras()).hasSize(1);
        assertThat(senso.getRegras().get(0).getDescricao()).isEqualTo("Regra de Teste 1");
    }

    @Test
    void testAddRegraToSenso() {
        // Adiciona uma nova regra ao senso e verifica se ela foi adicionada
    	Regra regra2 = Regra.builder().descricao("Regra de Teste 2").build();
        senso.getRegras().add(regra2);

        assertThat(senso.getRegras()).hasSize(2);
        assertThat(senso.getRegras().get(1).getDescricao()).isEqualTo("Regra de Teste 2");
    }

    @Test
    void testRemoveRegraFromSenso() {
        // Remove a regra e verifica se a lista fica vazia
        senso.getRegras().clear();

        assertThat(senso.getRegras()).isEmpty();
    }
}
