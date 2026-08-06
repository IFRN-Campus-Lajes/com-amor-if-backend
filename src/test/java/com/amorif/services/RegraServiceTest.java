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
}

