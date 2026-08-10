package com.amorif.controller;

import com.amorif.config.security.JWTTokenProviderMock;
import com.amorif.config.security.TestSecurityConfig;
import com.amorif.entities.Regra;
import com.amorif.entities.Role;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.TokenRepository;
import com.amorif.services.RegraService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@WebMvcTest(RegraController.class)
@Import({TestSecurityConfig.class, JWTTokenProviderMock.class})
public class RegraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegraService regraService;
    
    @MockBean
    private TokenRepository tokenRepository;
    
    @MockBean
    private RegraRepository regraRepository;

    @Test
    public void listarTodas_DeveRetornarListaDeRegras() throws Exception {
        // Configurar dados simulados
        Regra regra1 = Regra.builder().descricao("1 ponto por livro emprestado").grupo("media_comparativa").categoria("Acervo e empréstimos").operacao("SUM").valorMinimo(1).build();
        Regra regra2 = Regra.builder().descricao("12 ponto por livro emprestado").operacao("SUB").valorMinimo(1).build();

        // Configurar comportamento do serviço
        given(regraService.listarTodas()).willReturn(Arrays.asList(regra1, regra2));

        // Executar e verificar a resposta
        mockMvc.perform(MockMvcRequestBuilders.get("/api/regras/listarTodas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].descricao", is("1 ponto por livro emprestado")))
                .andExpect(jsonPath("$[0].grupo", is("media_comparativa")))
                .andExpect(jsonPath("$[0].categoria", is("Acervo e empréstimos")));
    }
    
    @Test
    public void testListarRegrasPermitidasParaUsuario() throws Exception {
        // Mockando o contexto de segurança com uma role específica
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO"));
        User user = new User("testUser", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, authorities)
        );
        
        Role role1 = Role.builder().name("ROLE_BIBLIOTECA").build();
        Role role2 = Role.builder().name("ROLE_APOIO_ACADEMICO").build();
        
        Regra r1 = Regra.builder().id(1L).descricao("Regra 1").grupo("media_comparativa").categoria("Desempenho acadêmico").roles(Arrays.asList(role1)).build();
        Regra r2 = Regra.builder().id(1L).descricao("Regra 2").roles(Arrays.asList(role2)).build();


        // Mockando as regras permitidas
        List<Regra> regrasPermitidas = List.of(
            r1, r2
        );

        // Configurando o comportamento do serviço para retornar as regras permitidas
        Mockito.when(regraService.listarRegrasPermitidasParaUsuario()).thenReturn(regrasPermitidas);

        // Executando o teste
        mockMvc.perform(MockMvcRequestBuilders.get("/api/regras/permitidas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))  // Verifica se a resposta contém duas regras
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].descricao").value("Regra 1"))
                .andExpect(jsonPath("$[0].grupo").value("media_comparativa"))
                .andExpect(jsonPath("$[0].categoria").value("Desempenho acadêmico"))
                .andExpect(jsonPath("$[1].id").value(1L))
                .andExpect(jsonPath("$[1].descricao").value("Regra 2"));
    }
    
}
