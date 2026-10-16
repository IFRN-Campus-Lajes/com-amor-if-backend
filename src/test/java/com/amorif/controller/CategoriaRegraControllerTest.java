package com.amorif.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amorif.dto.request.CategoriaRegraDtoRequest;
import com.amorif.entities.CategoriaRegra;
import com.amorif.entities.Regra;
import com.amorif.entities.Senso;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.repository.CategoriaRegraRepository;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.SensoRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaRegraControllerTest {

    @Mock
    private CategoriaRegraRepository categoriaRepository;
    @Mock
    private RegraRepository regraRepository;
    @Mock
    private SensoRepository sensoRepository;

    private CategoriaRegraController controller;

    @BeforeEach
    void setUp() {
        controller = new CategoriaRegraController(categoriaRepository, regraRepository, sensoRepository);
    }

    @Test
    void devePermitirMesmoNomeEmSensosDiferentes() {
        Senso limpeza = Senso.builder().id(1L).descricao("Limpeza").build();
        Senso saude = Senso.builder().id(2L).descricao("Saúde").build();
        when(sensoRepository.findById(1L)).thenReturn(Optional.of(limpeza));
        when(sensoRepository.findById(2L)).thenReturn(Optional.of(saude));
        when(categoriaRepository.findByNomeIgnoreCaseAndSenso_Id("Geral", 1L)).thenReturn(Optional.empty());
        when(categoriaRepository.findByNomeIgnoreCaseAndSenso_Id("Geral", 2L)).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(CategoriaRegra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaRegra primeira = controller.criar(new CategoriaRegraDtoRequest("Geral", 1L, true));
        CategoriaRegra segunda = controller.criar(new CategoriaRegraDtoRequest("Geral", 2L, true));

        assertEquals(1L, primeira.getSenso().getId());
        assertEquals(2L, segunda.getSenso().getId());
    }

    @Test
    void deveRecusarMesmoNomeNoMesmoSenso() {
        Senso limpeza = Senso.builder().id(1L).descricao("Limpeza").build();
        CategoriaRegra existente = CategoriaRegra.builder().nome("Geral").senso(limpeza).build();
        when(sensoRepository.findById(1L)).thenReturn(Optional.of(limpeza));
        when(categoriaRepository.findByNomeIgnoreCaseAndSenso_Id("geral", 1L))
                .thenReturn(Optional.of(existente));

        assertThrows(InvalidArgumentException.class,
                () -> controller.criar(new CategoriaRegraDtoRequest("geral", 1L, true)));
    }

    @Test
    void deveRenomearCategoriaERegrasAssociadas() {
        Senso limpeza = Senso.builder().id(1L).descricao("Limpeza").build();
        CategoriaRegra existente = CategoriaRegra.builder().nome("Antiga").senso(limpeza).ativo(true).build();
        Regra regra = Regra.builder().categoria("Antiga").senso(limpeza).build();
        when(categoriaRepository.findByNomeIgnoreCaseAndSenso_Id("Antiga", 1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findByNomeIgnoreCaseAndSenso_Id("Nova", 1L)).thenReturn(Optional.empty());
        when(sensoRepository.findById(1L)).thenReturn(Optional.of(limpeza));
        when(regraRepository.existsByCategoriaIgnoreCaseAndSenso_Id("Antiga", 1L)).thenReturn(true);
        when(regraRepository.findByCategoriaIgnoreCaseAndSenso_Id("Antiga", 1L)).thenReturn(List.of(regra));
        when(categoriaRepository.save(any(CategoriaRegra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaRegra atualizada = controller.atualizar(1L, "Antiga",
                new CategoriaRegraDtoRequest("Nova", 1L, true));

        assertEquals("Nova", atualizada.getNome());
        assertEquals("Nova", regra.getCategoria());
        verify(regraRepository).flush();
    }

    @Test
    void deveRecusarInativacaoComRegrasAtivas() {
        Senso limpeza = Senso.builder().id(1L).descricao("Limpeza").build();
        CategoriaRegra existente = CategoriaRegra.builder().nome("Geral").senso(limpeza).ativo(true).build();
        when(categoriaRepository.findByNomeIgnoreCaseAndSenso_Id("Geral", 1L)).thenReturn(Optional.of(existente));
        when(sensoRepository.findById(1L)).thenReturn(Optional.of(limpeza));
        when(regraRepository.existsByCategoriaIgnoreCaseAndSenso_Id("Geral", 1L)).thenReturn(true);
        when(regraRepository.existsByCategoriaIgnoreCaseAndSenso_IdAndAtivoTrue("Geral", 1L)).thenReturn(true);

        InvalidArgumentException erro = assertThrows(InvalidArgumentException.class,
                () -> controller.atualizar(1L, "Geral", new CategoriaRegraDtoRequest("Geral", 1L, false)));

        assertEquals("Desative as regras ativas da categoria antes de inativá-la.", erro.getMessage());
    }
}
