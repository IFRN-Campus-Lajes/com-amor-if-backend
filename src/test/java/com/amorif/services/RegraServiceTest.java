package com.amorif.services;

import com.amorif.entities.Regra;
import com.amorif.entities.Role;
import com.amorif.entities.User;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.RoleRepository;
import com.amorif.services.impl.RegraServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class RegraServiceTest {

    @Mock
    private RegraRepository regraRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RegraServiceImpl regraService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void listarTodas_DeveRetornarListaDeRegras() {
        // Configurar dados simulados
        Regra regra1 =  Regra.builder().descricao("1 ponto por livro emprestado").operacao("SUM").valorMinimo(1).build();
        Regra regra2 = Regra.builder().descricao("12 ponto por livro emprestado").operacao("SUB").valorMinimo(1).build();

        // Simular comportamento do repository
        when(regraRepository.findAll()).thenReturn(Arrays.asList(regra1, regra2));

        // Executar o teste
        List<Regra> regras = regraService.listarTodas();

        // Verificar o resultado
        assertEquals(2, regras.size());
        assertEquals("1 ponto por livro emprestado", regras.get(0).getDescricao());
    }

    @Test
    void administradorVisualizaTodasAsRegrasManuaisMesmoComVinculoLegadoIncompleto() {
        Role administrador = Role.builder().name("ROLE_ADMINISTRADOR").build();
        Role assistenciaEstudantil = Role.builder().name("ROLE_ASSISTENCIA_ESTUDANTIL").build();
        Role sistema = Role.builder().name("ROLE_SISTEMA").build();
        User user = User.builder().matricula("admin")
                .funcoes(new HashSet<>(List.of(administrador))).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));

        Regra avaliacaoBiomedica = Regra.builder()
                .descricao("2 pontos por aluno em avaliação biomédica ou odontológica")
                .roles(List.of(assistenciaEstudantil)).build();
        Regra automatica = Regra.builder().descricao("Regra automática")
                .roles(List.of(sistema, administrador)).build();
        when(regraRepository.findAll()).thenReturn(List.of(avaliacaoBiomedica, automatica));

        List<Regra> regras = regraService.listarRegrasPermitidasParaUsuario();

        assertEquals(List.of(avaliacaoBiomedica), regras);
    }
}

