package com.amorif.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RegraTest {

    private Regra regra;
    private Senso senso;
    private TipoRegra tipoRegra;
    private Role role1;
    private Role role2;

    @BeforeEach
    void setUp() {
        // Configuração das entidades relacionadas
        senso = Senso.builder()
                .id(1L)
                .descricao("Senso de Teste")
                .build();

        tipoRegra = TipoRegra.builder()
                .id(1L)
                .fixo(true)
                .temAluno(true)
                .frequencia(5)
                .build();

        // Criação dos objetos Role
        role1 = Role.builder().name(RoleEnum.ROLE_ALUNO.toString()).build();

        role2 = Role.builder().name(RoleEnum.ROLE_ADMINISTRADOR.toString()).build();

        // Associação dos roles à regra
        List<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);

        // Criação do objeto Regra com os dados iniciais e associação de roles
        regra = Regra.builder()
                .id(1L)
                .descricao("Regra de Teste")
                .grupo("Biblioteca")
                .categoria("Acervo e empréstimos")
                .valorMinimo(10)
                .valorMaximo(20)
                .senso(senso)
                .tipoRegra(tipoRegra)
                .roles(roles)
                .build();
    }

    @Test
    void testRegraAttributes() {
        // Verifica se os atributos foram atribuídos corretamente
        assertThat(regra.getId()).isEqualTo(1L);
        assertThat(regra.getDescricao()).isEqualTo("Regra de Teste");
        assertThat(regra.getGrupo()).isEqualTo("Biblioteca");
        assertThat(regra.getCategoria()).isEqualTo("Acervo e empréstimos");
        assertThat(regra.getValorMinimo()).isEqualTo(10);
        assertThat(regra.getValorMaximo()).isEqualTo(20);
    }

    @Test
    void testRegraRolesAssociation() {
        // Verifica se a associação com Role está correta
        assertThat(regra.getRoles()).isNotNull();
        assertThat(regra.getRoles()).hasSize(2);
        assertThat(regra.getRoles().get(0).getName()).isEqualTo("ROLE_ALUNO");
        assertThat(regra.getRoles().get(1).getName()).isEqualTo("ROLE_ADMINISTRADOR");
    }
}
