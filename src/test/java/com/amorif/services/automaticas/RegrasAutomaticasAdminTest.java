package com.amorif.services.automaticas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.amorif.dto.request.RegraAdminDtoRequest;
import com.amorif.entities.*;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.repository.*;
import com.amorif.services.impl.RegraAdminServiceImpl;

class RegrasAutomaticasAdminTest {
    final RegraRepository regras = mock(RegraRepository.class);
    final PontuacaoRepository pontos = mock(PontuacaoRepository.class);
    final SensoRepository sensos = mock(SensoRepository.class);
    final TipoRegraRepository tipos = mock(TipoRegraRepository.class);
    final RegraAdminServiceImpl service = new RegraAdminServiceImpl(regras, pontos, sensos, tipos,
        mock(RoleRepository.class), mock(CategoriaRegraRepository.class));

    @Test void rejeitaCriacaoEConversaoParaTipoAutomatico() {
        when(sensos.findById(1L)).thenReturn(Optional.of(Senso.builder().id(1L).descricao("Limpeza").build()));
        when(tipos.findById(1L)).thenReturn(Optional.of(TipoRegra.builder().automatico(true).build()));
        var request = new RegraAdminDtoRequest("Bônus", null, 1L, "Bônus", "SUM", 20, 20, 1L, 1L, List.of(), true);
        assertThrows(InvalidArgumentException.class, () -> service.criar(request));
        Regra manual = Regra.builder().id(2L).tipoRegra(TipoRegra.builder().build()).build();
        when(regras.findById(2L)).thenReturn(Optional.of(manual));
        assertThrows(InvalidArgumentException.class, () -> service.atualizar(2L, request));
        verify(regras, never()).save(any());
    }
    @Test void rejeitaEdicaoEDesativacaoMasPreservaConsulta() {
        Regra automatica = Regra.builder().id(1L).tipoRegra(TipoRegra.builder().automatico(true).build())
            .descricao("Bônus").senso(Senso.builder().descricao("Limpeza").build()).roles(List.of()).build();
        when(regras.findById(1L)).thenReturn(Optional.of(automatica));
        assertThrows(InvalidArgumentException.class, () -> service.atualizar(1L, null));
        assertThrows(InvalidArgumentException.class, () -> service.alterarStatus(1L, false));
        when(regras.findAllByOrderBySenso_DescricaoAscCategoriaAscDescricaoAsc()).thenReturn(List.of(automatica));
        assertTrue(service.listar().getFirst().automatico());
        verify(regras, never()).save(any());
    }
}
