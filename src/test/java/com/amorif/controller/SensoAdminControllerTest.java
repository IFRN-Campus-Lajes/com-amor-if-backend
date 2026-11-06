package com.amorif.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amorif.dto.request.SensoDtoRequest;
import com.amorif.entities.Senso;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.exceptions.ReferencedSenseException;
import com.amorif.repository.CategoriaRegraRepository;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.SensoRepository;

@ExtendWith(MockitoExtension.class)
class SensoAdminControllerTest {
    @Mock
    private SensoRepository sensoRepository;
    @Mock
    private RegraRepository regraRepository;
    @Mock
    private CategoriaRegraRepository categoriaRepository;

    private SensoAdminController controller;

    @BeforeEach
    void setUp() {
        controller = new SensoAdminController(sensoRepository, regraRepository, categoriaRepository);
    }

    @Test
    void deveListarEmOrdemAlfabetica() {
        List<Senso> sensos = List.of(Senso.builder().descricao("Limpeza").build());
        when(sensoRepository.findAllByOrderByDescricaoAsc()).thenReturn(sensos);

        assertEquals(sensos, controller.listar());
    }

    @Test
    void deveCriarSensoAtivoComDescricaoNormalizada() {
        when(sensoRepository.findByDescricaoIgnoreCase("Novo senso")).thenReturn(Optional.empty());
        when(sensoRepository.save(any(Senso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Senso criado = controller.criar(new SensoDtoRequest("  Novo senso  "));

        assertEquals("Novo senso", criado.getDescricao());
        assertTrue(criado.isAtivo());
    }

    @Test
    void deveRecusarDescricaoDuplicadaIgnorandoMaiusculas() {
        Senso existente = Senso.builder().id(1L).descricao("Limpeza").build();
        when(sensoRepository.findByDescricaoIgnoreCase("limpeza")).thenReturn(Optional.of(existente));

        InvalidArgumentException erro = assertThrows(InvalidArgumentException.class,
                () -> controller.criar(new SensoDtoRequest("limpeza")));

        assertEquals("Já existe um senso com essa descrição.", erro.getMessage());
    }

    @Test
    void devePermitirDesativarSensoSemReferencias() {
        Senso senso = Senso.builder().id(1L).descricao("Novo").ativo(true).build();
        when(sensoRepository.findById(1L)).thenReturn(Optional.of(senso));
        when(sensoRepository.save(senso)).thenReturn(senso);

        Senso atualizado = controller.alterarStatus(1L, false);

        assertFalse(atualizado.isAtivo());
        verify(sensoRepository).save(senso);
    }

    @Test
    void deveManterAtivoSensoReferenciadoPorRegra() {
        Senso senso = Senso.builder().id(1L).descricao("Limpeza").ativo(true).build();
        when(sensoRepository.findById(1L)).thenReturn(Optional.of(senso));
        when(regraRepository.existsBySenso_Id(1L)).thenReturn(true);

        ReferencedSenseException erro = assertThrows(ReferencedSenseException.class,
                () -> controller.alterarStatus(1L, false));

        assertTrue(senso.isAtivo());
        assertEquals("Sensos referenciados por regras ou categorias devem permanecer ativos.", erro.getMessage());
    }

    @Test
    void deveManterAtivoSensoReferenciadoPorCategoria() {
        Senso senso = Senso.builder().id(1L).descricao("Limpeza").ativo(true).build();
        when(sensoRepository.findById(1L)).thenReturn(Optional.of(senso));
        when(categoriaRepository.existsBySenso_Id(1L)).thenReturn(true);

        assertThrows(ReferencedSenseException.class, () -> controller.alterarStatus(1L, false));
        assertTrue(senso.isAtivo());
    }
}
