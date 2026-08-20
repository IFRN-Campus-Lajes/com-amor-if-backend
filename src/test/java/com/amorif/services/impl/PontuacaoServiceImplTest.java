package com.amorif.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.amorif.config.security.JWTTokenProviderMock;
import com.amorif.config.security.TestSecurityConfig;
import com.amorif.dto.request.PontuacaoDtoRequest;
import com.amorif.dto.response.PontuacaoDtoResponse;
import com.amorif.entities.AnoLetivo;
import com.amorif.entities.FrequenciaRegraEnum;
import com.amorif.entities.Pontuacao;
import com.amorif.entities.Regra;
import com.amorif.entities.Role;
import com.amorif.entities.Senso;
import com.amorif.entities.TipoRegra;
import com.amorif.entities.Turma;
import com.amorif.entities.TurnoEnum;
import com.amorif.exceptions.AnnualRuleException;
import com.amorif.exceptions.AnnualRulePerStudentException;
import com.amorif.exceptions.BimonthlyRuleException;
import com.amorif.exceptions.BimonthlyRulePerStudentException;
import com.amorif.exceptions.ClosedSchoolYearException;
import com.amorif.exceptions.InvalidBimesterException;
import com.amorif.exceptions.InvalidExtraBimesterException;
import com.amorif.exceptions.InvalidFixedValueException;
import com.amorif.exceptions.InvalidTurnException;
import com.amorif.exceptions.RuleNotFoundException;
import com.amorif.exceptions.UserHasNoPermitedRoleException;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.TokenRepository;
import com.amorif.repository.TurmaRepository;
import com.amorif.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@WebMvcTest(PontuacaoServiceImpl.class)
@Import({ TestSecurityConfig.class, JWTTokenProviderMock.class })
public class PontuacaoServiceImplTest {

	@InjectMocks
	private PontuacaoServiceImpl pontuacaoService;

	@MockBean
	private TurmaRepository turmaRepository;

	@MockBean
	private RegraRepository regraRepository;

	@MockBean
	private TokenRepository tokenRepository;

	@MockBean
	private PontuacaoRepository pontuacaoRepository;

	@MockBean
	private AnoLetivoRepository anoLetivoRepository;

	@MockBean
	private UserRepository userRepository;

	private Turma turma;
	private Turma turma2;
	private Turma turma3;
	private Turma turma4;
	private Regra regra;
	private PontuacaoDtoRequest dtoRequest;
	private AnoLetivo anoAtual;

	private Senso ordenacaoSenso;
	private Senso limpezaSenso;
	private Regra regraOrdenacao;
	private Regra regraLimpeza;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		turma = new Turma();
		turma.setId(1L);
		turma.setTurno(TurnoEnum.MATUTINO.ordinal());

		turma2 = new Turma();
		turma2.setId(1L);
		turma2.setTurno(TurnoEnum.MATUTINO.ordinal());

		turma3 = new Turma();
		turma3.setId(1L);
		turma3.setTurno(TurnoEnum.VESPERTINO.ordinal());

		turma4 = new Turma();
		turma4.setId(1L);
		turma4.setTurno(TurnoEnum.VESPERTINO.ordinal());

		com.amorif.entities.User user1 = com.amorif.entities.User.builder().matricula("123456").build();

		Role roleWithPermission = new Role();
		roleWithPermission.setName("ROLE_APOIO_ACADEMICO");

		regra = new Regra();
		regra.setId(1L);
		regra.setAtivo(true);
		regra.setValorMinimo(0);
		regra.setValorMaximo(500);
		regra.setRoles(Arrays.asList(roleWithPermission));

		TipoRegra tr1 = TipoRegra.builder().id(1L).frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).bimestreExtra(false)
				.build();
		
		TipoRegra tr2 = TipoRegra.builder().id(1L).frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).bimestreExtra(false)
				.build();

		regra.setTipoRegra(tr1);

		anoAtual = AnoLetivo.builder().aberto(true).ano(2023).id(1L).build();
		turma.setAnoLetivo(anoAtual);
		turma2.setAnoLetivo(anoAtual);
		turma3.setAnoLetivo(anoAtual);
		turma4.setAnoLetivo(anoAtual);

		dtoRequest = new PontuacaoDtoRequest();
		dtoRequest.setIdTurma(1L);
		dtoRequest.setIdRegra(1L);
		dtoRequest.setPontos(10);
		dtoRequest.setMotivacao("Motivação Teste");
		dtoRequest.setBimestre(0);

		when(anoLetivoRepository.getLastActiveAnoLetivo()).thenReturn(anoAtual);
		when(turmaRepository.getReferenceById(dtoRequest.getIdTurma())).thenReturn(turma);
		when(turmaRepository.findAllByTurnoAndAnoLetivo(TurnoEnum.MATUTINO.ordinal(), anoAtual))
				.thenReturn(Arrays.asList(turma, turma2));
		when(regraRepository.getReferenceById(dtoRequest.getIdRegra())).thenReturn(regra);
		when(pontuacaoRepository.save(any(Pontuacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userRepository.findByMatricula("user")).thenReturn(Optional.of(user1));

		ordenacaoSenso = Senso.builder().descricao("Ordenação").build();
		limpezaSenso = Senso.builder().descricao("Limpeza").build();

		regraOrdenacao = Regra.builder().id(11L).descricao("Ordenação").senso(ordenacaoSenso).valorMinimo(20)
				.roles(Arrays.asList(Role.builder().name("ROLE_ADMINISTRADOR").build())).tipoRegra(tr2).build();
		regraLimpeza = Regra.builder().id(18L).descricao("Limpeza").senso(limpezaSenso).valorMinimo(20)
				.roles(Arrays.asList(Role.builder().name("ROLE_ADMINISTRADOR").build())).tipoRegra(tr2).build();
	}

	@Test
	public void testThrowPoints_UserWithPermission_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getPontos(), response.getPontos());
	}

	@Test
	void throwPoints_WhenThereIsNoOpenSchoolYear_ShouldNotRegisterPoints() {
		when(anoLetivoRepository.getLastActiveAnoLetivo()).thenReturn(null);

		assertThrows(ClosedSchoolYearException.class, () -> pontuacaoService.throwPoints(dtoRequest));

		verify(pontuacaoRepository, never()).save(any(Pontuacao.class));
		verifyNoInteractions(regraRepository);
	}

	@Test
	void throwPoints_WhenClassBelongsToAnotherSchoolYear_ShouldNotRegisterPoints() {
		AnoLetivo anoFechado = AnoLetivo.builder().id(2L).ano(2022).aberto(false).build();
		turma.setAnoLetivo(anoFechado);

		assertThrows(ClosedSchoolYearException.class, () -> pontuacaoService.throwPoints(dtoRequest));

		verify(pontuacaoRepository, never()).save(any(Pontuacao.class));
	}

	@Test
	void currentSchoolYearQueries_WhenAllYearsAreClosed_ShouldReturnEmptyLists() {
		when(anoLetivoRepository.getLastActiveAnoLetivo()).thenReturn(null);

		assertTrue(pontuacaoService.pontosByLastActiveYear().isEmpty());
		assertTrue(pontuacaoService.pointsToValidate().isEmpty());
		assertTrue(pontuacaoService.appliedPointsOfLastActiveYear().isEmpty());
		assertTrue(pontuacaoService.cancelledPointsOfLastActiveYear().isEmpty());
		assertTrue(pontuacaoService.pontosByLoggedUser().isEmpty());
		verify(pontuacaoRepository, never()).pontosByAno(anyLong());
	}

	@Test
	void throwAutoPoints_WhenThereIsNoOpenSchoolYear_ShouldNotReprocessPoints() {
		when(anoLetivoRepository.getLastActiveAnoLetivo()).thenReturn(null);

		assertThrows(ClosedSchoolYearException.class, () -> pontuacaoService.throwAutoPoints(dtoRequest));

		verify(pontuacaoRepository, never()).save(any(Pontuacao.class));
		verifyNoInteractions(regraRepository);
		verify(turmaRepository, never()).findTurmasQualificadasParaBonus(anyLong(), anyList(), anyInt(),
				eq(anoAtual));
	}

	@Test
	void deletePontuacao_WhenItsSchoolYearIsClosed_ShouldNotDeletePoints() {
		AnoLetivo anoFechado = AnoLetivo.builder().id(1L).ano(2023).aberto(false).build();
		Pontuacao pontuacao = new Pontuacao();
		pontuacao.setAnoLetivo(anoFechado);
		dtoRequest.setContador(1);
		when(pontuacaoRepository.findByContadorAndTurma_Id(dtoRequest.getContador(), dtoRequest.getIdTurma()))
				.thenReturn(pontuacao);

		assertThrows(ClosedSchoolYearException.class, () -> pontuacaoService.deletePontuacao(dtoRequest));

		verify(pontuacaoRepository, never()).deleteByContadorAndTurma_Id(any(), anyLong());
	}

	@Test
	public void testThrowPoints_UserWithoutPermission_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que NÃO permite o lançamento
		User userWithoutPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_BIBLIOTECARIO")));
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				userWithoutPermission, null, userWithoutPermission.getAuthorities()));

		// Verifica se o lançamento falha ao usuário não ter permissão
		assertThrows(UserHasNoPermitedRoleException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});
	}

	@Test
	public void testThrowPoints_BimesterValid_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getBimestre(), response.getBimestre());
	}

	@Test
	public void testThrowPoints_BimesterInvalid_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		dtoRequest.setBimestre(10);

		// Verifica se o lançamento falha com bimestre inválido
		assertThrows(InvalidBimesterException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});
	}

	@Test
	public void testThrowPoints_ExtraBimesterValid_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		TipoRegra tr2 = TipoRegra.builder().id(1L).bimestreExtra(true).frequencia(FrequenciaRegraEnum.AVULSO.ordinal())
				.build();
		regra.setTipoRegra(tr2);
		dtoRequest.setBimestre(4);

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();
		;

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getBimestre(), response.getBimestre());
	}

	@Test
	public void testThrowPoints_ExtraBimesterInvalid_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		TipoRegra tr2 = TipoRegra.builder().id(1L).bimestreExtra(true).build();
		regra.setTipoRegra(tr2);
		dtoRequest.setBimestre(0);

		// Verifica se o lançamento falha com bimestre inválido
		assertThrows(InvalidExtraBimesterException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});
	}

	@Test
	public void testThrowPoints_FixedValueValid_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getPontos(), response.getPontos());
	}

	@Test
	public void testThrowPoints_FixedValueInvalid_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(8);

		// Verifica se o lançamento falha com pontos inválidos
		assertThrows(InvalidFixedValueException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});
	}

	@Test
	public void testThrowPoints_PerBimester_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByBimesterAndRule(0, regra.getId(), turma.getId())).thenReturn(false);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal())
				.build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getBimestre(), response.getBimestre());
	}

	@Test
	public void testThrowPoints_PerBimester_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByBimesterAndRule(0, regra.getId(), turma.getId())).thenReturn(true);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal())
				.temAluno(false).build();
		regra.setTipoRegra(tr2);
		regra.setCategoria("Avaliação da organização");
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);

		// Verifica se a exceção BimonthlyRuleException é lançada
		assertThrows(BimonthlyRuleException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});

		// Verifica se o método do repositório foi chamado com os parâmetros corretos
		verify(pontuacaoRepository).existsByBimesterAndRule(0, regra.getId(), turma.getId());
		verify(pontuacaoRepository, never()).existsByBimesterAndGroup(anyInt(), anyLong(), anyString(), any());
	}

	@Test
	public void testThrowPoints_PerYear_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByYearAndRule(1L, regra.getId(), turma.getId())).thenReturn(false);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.ANUAL.ordinal()).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getBimestre(), response.getBimestre());
	}

	@Test
	public void testThrowPoints_PerYear_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByYearAndRule(1L, regra.getId(), turma.getId())).thenReturn(true);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.ANUAL.ordinal()).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);

		// Verifica se a exceção BimonthlyRuleException é lançada
		assertThrows(AnnualRuleException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});

		// Verifica se o método do repositório foi chamado com os parâmetros corretos
		verify(pontuacaoRepository).existsByYearAndRule(1L, regra.getId(), turma.getId());
	}

	@Test
	public void testThrowPoints_PerTurn_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByYearAndRule(1L, regra.getId(), turma.getId())).thenReturn(true);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.AVULSO.ordinal())
				.porTurno(true).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);
		dtoRequest.setTurno(TurnoEnum.MATUTINO.ordinal());

		List<PontuacaoDtoResponse> response = pontuacaoService.throwPoints(dtoRequest);

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(2, response.size());
	}

	@Test
	public void testThrowPoints_InactiveRule_ShouldNotRegisterPoints() {
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));
		regra.setAtivo(false);

		assertThrows(RuleNotFoundException.class, () -> pontuacaoService.throwPoints(dtoRequest));
		verify(pontuacaoRepository, never()).save(any(Pontuacao.class));
	}

	@Test
	public void testThrowPoints_VariablePerTurn_ShouldRegisterValueForEveryClass() {
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		TipoRegra tipoVariavelPorTurno = TipoRegra.builder().id(10L).fixo(false)
				.frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).porTurno(true).build();
		regra.setTipoRegra(tipoVariavelPorTurno);
		regra.setValorMinimo(10);
		regra.setValorMaximo(50);
		dtoRequest.setPontos(37);
		dtoRequest.setTurno(TurnoEnum.MATUTINO.ordinal());

		List<PontuacaoDtoResponse> response = pontuacaoService.throwPoints(dtoRequest);

		assertEquals(2, response.size());
		response.forEach(pontuacao -> assertEquals(37, pontuacao.getPontos()));
	}

	@Test
	public void testThrowPoints_PerTurn_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByYearAndRule(1L, regra.getId(), turma.getId())).thenReturn(true);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.AVULSO.ordinal())
				.porTurno(true).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);
		dtoRequest.setTurno(98);

		// Verifica se a exceção BimonthlyRuleException é lançada
		assertThrows(InvalidTurnException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});
	}

	@Test
	public void testThrowPoints_PerBimesterPerStudent_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByBimesterAndRulePerStudent(0, regra.getId(), turma.getId(), "123"))
				.thenReturn(false);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal())
				.build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);
		dtoRequest.setMatriculaAluno("123");

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getMatriculaAluno(), response.getMatriculaAluno());
	}

	@Test
	public void testThrowPoints_PerBimesterPerStudent_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByBimesterAndRulePerStudent(0, regra.getId(), turma.getId(), "20151204010002"))
				.thenReturn(true);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal())
				.temAluno(true).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);
		dtoRequest.setMatriculaAluno("20151204010002");

		// Verifica se a exceção BimonthlyRuleException é lançada
		assertThrows(BimonthlyRulePerStudentException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});

		// Verifica se o método do repositório foi chamado com os parâmetros corretos
		verify(pontuacaoRepository).existsByBimesterAndRulePerStudent(0, regra.getId(), turma.getId(),
				"20151204010002");
	}

	@Test
	public void testThrowPoints_PerYearPerStudent_ShouldRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByYearAndRulePerStudent(1L, regra.getId(), turma.getId(), "123"))
				.thenReturn(false);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.ANUAL.ordinal()).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);
		dtoRequest.setMatriculaAluno("123");

		PontuacaoDtoResponse response = pontuacaoService.throwPoints(dtoRequest).getFirst();

		// Verifica se o lançamento foi feito com sucesso
		assertNotNull(response);
		assertEquals(dtoRequest.getMatriculaAluno(), response.getMatriculaAluno());
	}

	@Test
	public void testThrowPoints_PerYearPerStudent_ShouldNotRegisterPoints() {
		// Mockando o contexto de segurança com uma role que permite o lançamento
		User userWithPermission = new User("user", "password",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_APOIO_ACADEMICO")));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

		// Mockando o comportamento do repository
		when(pontuacaoRepository.existsByYearAndRulePerStudent(1L, regra.getId(), turma.getId(), "20151204010002"))
				.thenReturn(true);

		// Configurando a regra e o DTO
		TipoRegra tr2 = TipoRegra.builder().id(1L).fixo(true).frequencia(FrequenciaRegraEnum.ANUAL.ordinal())
				.temAluno(true).build();
		regra.setTipoRegra(tr2);
		regra.setValorMinimo(10);
		dtoRequest.setBimestre(0);
		dtoRequest.setPontos(10);
		dtoRequest.setMatriculaAluno("20151204010002");

		// Verifica se a exceção BimonthlyRuleException é lançada
		assertThrows(AnnualRulePerStudentException.class, () -> {
			pontuacaoService.throwPoints(dtoRequest);
		});

		// Verifica se o método do repositório foi chamado com os parâmetros corretos
		verify(pontuacaoRepository).existsByYearAndRulePerStudent(1L, regra.getId(), turma.getId(), "20151204010002");
	}

	@Test
	void testThrowAutoPoints_WhenTurmasQualificadas_ShouldSavePontuacoes() {
	    // Mockando o contexto de segurança
	    User userWithPermission = new User("user", "password",
	            Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
	    SecurityContextHolder.getContext().setAuthentication(
	            new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

	    // Configurar dados de teste
	    Turma turma1 = Turma.builder().id(1L).nome("Turma A").build();
	    List<Turma> turmasLimpeza = Arrays.asList(turma1);
	    List<Turma> turmasOrdenacao = Arrays.asList(turma1);

	    // Mock para regras específicas
	    when(turmaRepository.findTurmasQualificadasParaBonus(16L, Arrays.asList(13L, 14L, 15L, 17L, 18L), 1,
	            anoAtual))
	            .thenReturn(turmasLimpeza);
	    when(turmaRepository.findTurmasQualificadasParaBonus(10L, Arrays.asList(7L, 8L, 9L, 12L, 11L), 1,
	            anoAtual))
	            .thenReturn(turmasOrdenacao);

	    when(regraRepository.findById(18L)).thenReturn(Optional.of(regraLimpeza));
	    when(regraRepository.findById(11L)).thenReturn(Optional.of(regraOrdenacao));
	    
	    when(regraRepository.getReferenceById(18L)).thenReturn(regraLimpeza);
	    when(regraRepository.getReferenceById(11L)).thenReturn(regraOrdenacao);

	    // Chamar o método
	    PontuacaoDtoRequest dtoRequest = new PontuacaoDtoRequest();
	    dtoRequest.setBimestre(1);

	    pontuacaoService.throwAutoPoints(dtoRequest);

	    // Verificar que as pontuações foram salvas corretamente
	    ArgumentCaptor<Pontuacao> pontuacaoCaptor = ArgumentCaptor.forClass(Pontuacao.class);
	    verify(pontuacaoRepository, times(2)).save(pontuacaoCaptor.capture());

	    List<Pontuacao> savedPontuacoes = pontuacaoCaptor.getAllValues();
	    assertEquals(2, savedPontuacoes.size());
	    assertEquals(18L, savedPontuacoes.get(0).getRegra().getId());
	    assertEquals(11L, savedPontuacoes.get(1).getRegra().getId());
	}
	
	@Test
	void testThrowAutoPoints_WhenNoTurmasQualificadas_ShouldNotSavePontuacoes() {
	    // Mockando o contexto de segurança
	    User userWithPermission = new User("user", "password",
	            Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
	    SecurityContextHolder.getContext().setAuthentication(
	            new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

	    // Mock para listas vazias
	    when(turmaRepository.findTurmasQualificadasParaBonus(16L, Arrays.asList(13L, 14L, 15L, 17L), 1,
	            anoAtual))
	            .thenReturn(Collections.emptyList());
	    when(turmaRepository.findTurmasQualificadasParaBonus(10L, Arrays.asList(7L, 8L, 9L, 12L), 1,
	            anoAtual))
	            .thenReturn(Collections.emptyList());
	    
	    when(regraRepository.findById(18L)).thenReturn(Optional.of(regraLimpeza));
	    when(regraRepository.findById(11L)).thenReturn(Optional.of(regraOrdenacao));

	    PontuacaoDtoRequest dtoRequest = new PontuacaoDtoRequest();
	    dtoRequest.setBimestre(1);

	    List<PontuacaoDtoResponse> responses = pontuacaoService.throwAutoPoints(dtoRequest);

	    // Verificar que nenhuma pontuação foi salva
	    verify(pontuacaoRepository, never()).save(any(Pontuacao.class));
	    assertTrue(responses.isEmpty());
	}
	
	@Test
	void testThrowAutoPoints_WhenOnlyOneRegraHasTurmasQualificadas_ShouldSavePontuacoesForThatRegra() {
	    // Mockando o contexto de segurança
	    User userWithPermission = new User("user", "password",
	            Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
	    SecurityContextHolder.getContext().setAuthentication(
	            new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

	    Turma turma1 = Turma.builder().id(1L).nome("Turma A").build();
	    List<Turma> turmasOrdenacao = Arrays.asList(turma1);

	    // Mock para apenas uma regra qualificada
	    when(turmaRepository.findTurmasQualificadasParaBonus(16L, Arrays.asList(13L, 14L, 15L, 17L, 18L), 1,
	            anoAtual))
	            .thenReturn(Collections.emptyList());
	    when(turmaRepository.findTurmasQualificadasParaBonus(10L, Arrays.asList(7L, 8L, 9L, 12L, 11L), 1,
	            anoAtual))
	            .thenReturn(turmasOrdenacao);

	    when(regraRepository.findById(18L)).thenReturn(Optional.of(regraLimpeza));
	    when(regraRepository.findById(11L)).thenReturn(Optional.of(regraOrdenacao));
	    
	    when(regraRepository.getReferenceById(18L)).thenReturn(regraLimpeza);
	    when(regraRepository.getReferenceById(11L)).thenReturn(regraOrdenacao);
	    
	    PontuacaoDtoRequest dtoRequest = new PontuacaoDtoRequest();
	    dtoRequest.setBimestre(1);

	    pontuacaoService.throwAutoPoints(dtoRequest);

	    // Verificar que apenas pontuações da regra ordenação foram salvas
	    ArgumentCaptor<Pontuacao> pontuacaoCaptor = ArgumentCaptor.forClass(Pontuacao.class);
	    verify(pontuacaoRepository, times(1)).save(pontuacaoCaptor.capture());

	    List<Pontuacao> savedPontuacoes = pontuacaoCaptor.getAllValues();
	    assertEquals(1, savedPontuacoes.size());
	    assertEquals(11L, savedPontuacoes.get(0).getRegra().getId());
	}
	
	@Test
	void testThrowAutoPoints_WhenRegraNotFound_ShouldThrowException() {
	    when(regraRepository.findById(18L)).thenReturn(Optional.empty());

	    PontuacaoDtoRequest dtoRequest = new PontuacaoDtoRequest();
	    dtoRequest.setBimestre(1);

	    assertThrows(RuleNotFoundException.class, () -> pontuacaoService.throwAutoPoints(dtoRequest));
	}

	@Test
	void testThrowAutoPoints_WhenTurmaWithInvalidId_ShouldNotSave() {
	    // Mockando o contexto de segurança
	    User userWithPermission = new User("user", "password",
	            Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")));
	    SecurityContextHolder.getContext().setAuthentication(
	            new UsernamePasswordAuthenticationToken(userWithPermission, null, userWithPermission.getAuthorities()));

	    Turma turmaInvalida = Turma.builder().id(null).nome("Turma X").build();
	    List<Turma> turmas = Arrays.asList(turmaInvalida);

	    when(turmaRepository.findTurmasQualificadasParaBonus(16L, Arrays.asList(13L, 14L, 15L, 17L), 1,
	            anoAtual))
	            .thenReturn(turmas);

	    when(regraRepository.findById(11L)).thenReturn(Optional.of(regraLimpeza));
	    when(regraRepository.findById(18L)).thenReturn(Optional.of(regraOrdenacao));

	    PontuacaoDtoRequest dtoRequest = new PontuacaoDtoRequest();
	    dtoRequest.setBimestre(1);

	    pontuacaoService.throwAutoPoints(dtoRequest);

	    verify(pontuacaoRepository, never()).save(any(Pontuacao.class));
	}

}
