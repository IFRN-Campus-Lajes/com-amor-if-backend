package com.amorif.services;

import com.amorif.entities.Regra;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.RoleRepository;
import com.amorif.services.impl.RegraServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.amorif.entities.Role;
import com.amorif.entities.User;
import java.util.Set;
import java.util.Arrays;
import java.util.List;

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
        when(regraRepository.findByAtivoTrue()).thenReturn(Arrays.asList(regra1, regra2));

        // Executar o teste
        List<Regra> regras = regraService.listarTodas();

        // Verificar o resultado
        assertEquals(2, regras.size());
        assertEquals("1 ponto por livro emprestado", regras.get(0).getDescricao());
    }

	@Test
	public void listarTodas_DeveConsultarSomenteRegrasAtivas() {
		when(regraRepository.findByAtivoTrue()).thenReturn(List.of());

		regraService.listarTodas();

		org.mockito.Mockito.verify(regraRepository).findByAtivoTrue();
	}

    @Test
    void administradorDeveVisualizarTodaRegraManualAtiva() {
        Role administrador = Role.builder().id(1L).name("ROLE_ADMINISTRADOR").build();
        Role bibliotecario = Role.builder().id(2L).name("ROLE_BIBLIOTECARIO").build();
        Role sistema = Role.builder().id(3L).name("ROLE_SISTEMA").build();
        User usuario = User.builder().matricula("admin").funcoes(Set.of(administrador)).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        Regra manual = Regra.builder().descricao("Regra nova").roles(List.of(bibliotecario)).build();
        Regra automatica = Regra.builder().descricao("Regra automática").roles(List.of(sistema)).build();
        when(regraRepository.findByAtivoTrue()).thenReturn(List.of(manual, automatica));

        List<Regra> permitidas = regraService.listarRegrasPermitidasParaUsuario();

        assertEquals(List.of(manual), permitidas);
    }
}

