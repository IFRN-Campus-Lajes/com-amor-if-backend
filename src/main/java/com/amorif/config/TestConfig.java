package com.amorif.config;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amorif.entities.AnoLetivo;
import com.amorif.entities.FrequenciaRegraEnum;
import com.amorif.entities.Pontuacao;
import com.amorif.entities.Role;
import com.amorif.entities.RoleEnum;
import com.amorif.entities.Senso;
import com.amorif.entities.Regra;
import com.amorif.entities.TipoRegra;
import com.amorif.entities.Turma;
import com.amorif.entities.TurnoEnum;
import com.amorif.entities.User;
import com.amorif.repository.AnoLetivoRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.repository.RoleRepository;
import com.amorif.repository.SensoRepository;
import com.amorif.repository.TipoRegraRepository;
import com.amorif.repository.TurmaRepository;
import com.amorif.repository.UserRepository;
import com.amorif.repository.RegraRepository;

/**
 * @author osailton
 */

@Configuration
@Profile({ "test", "dev" })
public class TestConfig implements CommandLineRunner {

	private final AnoLetivoRepository anoLetivoRepository;
	private final TurmaRepository turmaRepository;
	private final PontuacaoRepository pontuacaoRepository;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final SensoRepository sensoRepository;
	private final TipoRegraRepository tipoRegraRepository;
	private final RegraRepository regraRepository;

	public TestConfig(AnoLetivoRepository anoLetivoRepository, TurmaRepository turmaRepository,
			PontuacaoRepository pontuacaoRepository, RoleRepository roleRepository, UserRepository userRepository,
			SensoRepository sensoRepository, TipoRegraRepository tipoRegraRepository, RegraRepository regraRepository) { // Adicione
																															// TipoRegraRepository
																															// no
																															// construtor
		this.anoLetivoRepository = anoLetivoRepository;
		this.turmaRepository = turmaRepository;
		this.pontuacaoRepository = pontuacaoRepository;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.sensoRepository = sensoRepository;
		this.tipoRegraRepository = tipoRegraRepository;
		this.regraRepository = regraRepository;
	}

	@Override
	public void run(String... args) throws Exception {
//		Create Roles
		Role r1 = Role.builder().name(RoleEnum.ROLE_ADMINISTRADOR.toString()).build();
		Role r2 = Role.builder().name(RoleEnum.ROLE_AVAL.toString()).build();
		Role r4 = Role.builder().name(RoleEnum.ROLE_ALUNO.toString()).build();
		Role r5 = Role.builder().name(RoleEnum.ROLE_BIBLIOTECARIO.toString()).build();
		Role r6 = Role.builder().name(RoleEnum.ROLE_APOIO_ACADEMICO.toString()).build();
		Role r7 = Role.builder().name(RoleEnum.ROLE_SISTEMA.toString()).build();
		Role r8 = Role.builder().name(RoleEnum.ROLE_DOCENTE.toString()).build();
		Role r9 = Role.builder().name(RoleEnum.ROLE_ASSESSORIA_PEDAGOGICA.toString()).build();
		Role r10 = Role.builder().name(RoleEnum.ROLE_COEXPEIN.toString()).build();
		Role r11 = Role.builder().name(RoleEnum.ROLE_COORDENADOR_CURSO.toString()).build();
		Role r12 = Role.builder().name(RoleEnum.ROLE_ASSESSORIA_LABORATORIO.toString()).build();
		Role r13 = Role.builder().name(RoleEnum.ROLE_ASSISTENCIA_ESTUDANTIL.toString()).build();
		Role r14 = Role.builder().name(RoleEnum.ROLE_SEAC.toString()).build();
		roleRepository.saveAll(Arrays.asList(r1, r2, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14));

//		Create User
		User u1 = User.builder().nome("Teste").matricula("0101").funcoes(Set.of(r14, r8)).build();
		userRepository.saveAll(Arrays.asList(u1));

		User u2 = User.builder().nome("Teste").matricula("0102").funcoes(Set.of(r5, r6, r7, r8, r9, r10, r11, r12, r13))
				.build();
		userRepository.saveAll(Arrays.asList(u2));

//		Criar ano letivo
		AnoLetivo ano22 = AnoLetivo.builder().ano(2022).aberto(true).build();

		AnoLetivo ano23 = AnoLetivo.builder().ano(2023).aberto(true).build();
		anoLetivoRepository.saveAll(Arrays.asList(ano22, ano23));

//		Criar turmas
		Turma turmaA = Turma.builder().id(1L).anoLetivo(ano23).nome("ADM 1VA").descricao("Descricao da turma.")
				.turno(TurnoEnum.VESPERTINO.ordinal()).build();

		Turma turmaB = Turma.builder().id(2L).anoLetivo(ano23).nome("ADM 1VB").descricao("Descricao da turma.")
				.turno(TurnoEnum.VESPERTINO.ordinal()).build();

		Turma turmaC = Turma.builder().id(3L).anoLetivo(ano23).nome("INFO 1MA").descricao("Descricao da turma.")
				.turno(TurnoEnum.MATUTINO.ordinal()).build();

		Turma turmaD = Turma.builder().id(4L).anoLetivo(ano23).nome("INFO 2MA").descricao("Descricao da turma.")
				.turno(TurnoEnum.MATUTINO.ordinal()).build();

		Turma turmaE = Turma.builder().id(5L).anoLetivo(ano23).nome("ADM 4VA").descricao("Descricao da turma.")
				.turno(TurnoEnum.VESPERTINO.ordinal()).build();

		turmaRepository.saveAll(Arrays.asList(turmaA, turmaB, turmaC, turmaD, turmaE));

		// Criar sensos
		Senso senso1 = Senso.builder().descricao("Utilização").build();
		Senso senso2 = Senso.builder().descricao("Ordenação").build();
		Senso senso3 = Senso.builder().descricao("Limpeza").build();
		Senso senso4 = Senso.builder().descricao("Saúde").build();
		Senso senso5 = Senso.builder().descricao("Autodisciplina").build();
		sensoRepository.saveAll(Arrays.asList(senso1, senso2, senso3, senso4, senso5));

		// Criar tipos de regra
		TipoRegra tipo1 = TipoRegra.builder().descricao("Valor Fixo").fixo(true).temAluno(false)
				.frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).build();
		TipoRegra tipo2 = TipoRegra.builder().descricao("Valor Fixo em bimestre extra por ano letivo").fixo(true)
				.temAluno(false).frequencia(FrequenciaRegraEnum.ANUAL.ordinal()).bimestreExtra(true).build();
		TipoRegra tipo3 = TipoRegra.builder().descricao("Valor Fixo automático por bimestre").fixo(true).temAluno(false)
				.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).automatico(true).build();
		TipoRegra tipo4 = TipoRegra.builder().descricao("Valor Fixo por turno").fixo(true).temAluno(false)
				.frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).porTurno(true).build();
		TipoRegra tipo5 = TipoRegra.builder().descricao("Valor Fixo por aluno por bimestre").fixo(true).temAluno(true)
				.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build();
		TipoRegra tipo6 = TipoRegra.builder().descricao("Valor Variável por bimestre").fixo(false).temAluno(false)
				.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build();
		TipoRegra tipo7 = TipoRegra.builder().descricao("Valor Fixo por aluno por ano letivo").fixo(true).temAluno(true)
				.frequencia(FrequenciaRegraEnum.ANUAL.ordinal()).build();
		TipoRegra tipo8 = TipoRegra.builder().descricao("Valor Variável").fixo(false).temAluno(false)
				.frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).build();
		TipoRegra tipo9 = TipoRegra.builder().descricao("Valor Fixo por bimestre").fixo(true).temAluno(false)
				.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build();

		tipoRegraRepository.saveAll(Arrays.asList(tipo1, tipo2, tipo3, tipo4, tipo5, tipo6, tipo7, tipo8, tipo9));

		// População de regras
		// Obter instâncias de Senso
		Senso utilizacao = sensoRepository.findByDescricao("Utilização");
		Senso ordenacao = sensoRepository.findByDescricao("Ordenação");
		Senso limpeza = sensoRepository.findByDescricao("Limpeza");
		Senso saude = sensoRepository.findByDescricao("Saúde");
		Senso autodisciplina = sensoRepository.findByDescricao("Autodisciplina");

		// Obter instâncias de TipoRegra
		TipoRegra tipoFixo = tipoRegraRepository.findByDescricao("Valor Fixo");
		TipoRegra tipoFixoBimestreExtra = tipoRegraRepository
				.findByDescricao("Valor Fixo em bimestre extra por ano letivo");
		TipoRegra tipoAutomatico = tipoRegraRepository.findByDescricao("Valor Fixo automático por bimestre");
		TipoRegra tipoPorTurno = tipoRegraRepository.findByDescricao("Valor Fixo por turno");
		TipoRegra tipoPorAlunoBimestre = tipoRegraRepository.findByDescricao("Valor Fixo por aluno por bimestre");
		TipoRegra tipoVariavelBimestre = tipoRegraRepository.findByDescricao("Valor Variável por bimestre");
		TipoRegra tipoPorAlunoAno = tipoRegraRepository.findByDescricao("Valor Fixo por aluno por ano letivo");
		TipoRegra tipoVariavel = tipoRegraRepository.findByDescricao("Valor Variável");
		TipoRegra tipoFixoPorBimestre = tipoRegraRepository.findByDescricao("Valor Fixo por bimestre");

		// Obter instâncias de Role
		Role bibliotecario = roleRepository.getByName("ROLE_BIBLIOTECARIO");
		Role apoioAcademico = roleRepository.getByName("ROLE_APOIO_ACADEMICO");
		Role sistema = roleRepository.getByName("ROLE_SISTEMA");
		Role docente = roleRepository.getByName("ROLE_DOCENTE");
		Role assessoriaPedagogica = roleRepository.getByName("ROLE_ASSESSORIA_PEDAGOGICA");
		Role coexpein = roleRepository.getByName("ROLE_COEXPEIN");
		Role coordenadorCurso = roleRepository.getByName("ROLE_COORDENADOR_CURSO");
		Role assistenciaEstudantil = roleRepository.getByName("ROLE_ASSISTENCIA_ESTUDANTIL");
		Role assessoriaLaboratorio = roleRepository.getByName("ROLE_ASSESSORIA_LABORATORIO");
		Role administrador = roleRepository.getByName("ROLE_ADMINISTRADOR");
		Role seac = roleRepository.getByName("ROLE_SEAC");

		// População das regras
		List<Regra> regras = Arrays.asList(
				// Utilização - Bibliotecário - Positivas
				Regra.builder().descricao("1 ponto por livro emprestado").operacao("SUM").valorMinimo(1)
						.senso(utilizacao).tipoRegra(tipoFixo).roles(Arrays.asList(bibliotecario, administrador))
						.build(),
				Regra.builder().descricao("30 pontos por campanha de doação").operacao("SUM").valorMinimo(30)
						.senso(utilizacao).tipoRegra(tipoFixo).roles(Arrays.asList(bibliotecario, administrador))
						.build(),
				Regra.builder().descricao(
						"20 pontos no bimestre extra para a turma que mais tiver formado grupos de estudo no ano letivo")
						.operacao("SUM").valorMinimo(20).senso(utilizacao).tipoRegra(tipoFixoBimestreExtra)
						.roles(Arrays.asList(bibliotecario, administrador)).build(),

				// Utilização - Bibliotecário - Negativas
				Regra.builder().descricao("20 pontos por perda de livro").operacao("SUB").valorMinimo(20)
						.senso(utilizacao).tipoRegra(tipoFixo).roles(Arrays.asList(bibliotecario, administrador))
						.build(),
				Regra.builder().descricao("3 pontos por atraso de livro").operacao("SUB").valorMinimo(3)
						.senso(utilizacao).tipoRegra(tipoFixo).roles(Arrays.asList(bibliotecario, administrador))
						.build(),
				Regra.builder().descricao("5 pontos por avaria de livro").operacao("SUB").valorMinimo(5)
						.senso(utilizacao).tipoRegra(tipoFixo).roles(Arrays.asList(bibliotecario, administrador))
						.build(),
				Regra.builder().descricao("Pontos por trapaça").operacao("SUB").valorMinimo(1).senso(utilizacao)
						.tipoRegra(tipoVariavel).roles(Arrays.asList(bibliotecario, administrador)).build(),

				// Ordenação - Apoio Acadêmico - Positivas
				Regra.builder().descricao("0 pontos pela organização ruim").operacao("SUM").valorMinimo(0)
						.senso(ordenacao).tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador))
						.build(),
				Regra.builder().descricao("5 pontos pela organização mediana").operacao("SUM").valorMinimo(5)
						.senso(ordenacao).tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador))
						.build(),
				Regra.builder().descricao("8 pontos pela organização boa").operacao("SUM").valorMinimo(8)
						.senso(ordenacao).tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador))
						.build(),
				Regra.builder().descricao("10 pontos pela organização excelente").operacao("SUM").valorMinimo(10)
						.senso(ordenacao).tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador))
						.build(),

				// Ordenação - Sistema - Positivas
				Regra.builder().descricao("20 pontos ao fim de cada bimestre se a turma tiver 100% de nota 10")
						.operacao("SUM").valorMinimo(20).senso(ordenacao).tipoRegra(tipoAutomatico)
						.roles(Arrays.asList(sistema, administrador)).build(),

				// Ordenação - Assistência Estudantil, Apoio Acadêmico e ASLAB - Negativas
				Regra.builder().descricao("10 pontos por desordem para todas as turmas do turno").operacao("SUB")
						.valorMinimo(10).senso(ordenacao).tipoRegra(tipoPorTurno)
						.roles(Arrays.asList(assistenciaEstudantil, apoioAcademico, assessoriaLaboratorio,
								administrador))
						.build(),

				// Limpeza - Apoio Acadêmico - Positivas
				Regra.builder().descricao("0 pontos pela limpeza ruim").operacao("SUM").valorMinimo(0).senso(limpeza)
						.tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador)).build(),
				Regra.builder().descricao("5 pontos pela limpeza mediana").operacao("SUM").valorMinimo(5).senso(limpeza)
						.tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador)).build(),
				Regra.builder().descricao("8 pontos pela limpeza boa").operacao("SUM").valorMinimo(8).senso(limpeza)
						.tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador)).build(),
				Regra.builder().descricao("10 pontos pela limpeza excelente").operacao("SUM").valorMinimo(10)
						.senso(limpeza).tipoRegra(tipoFixo).roles(Arrays.asList(apoioAcademico, administrador)).build(),

				// Limpeza - Apoio Acadêmico - Negativas
				Regra.builder().descricao("10 pontos por falta de limpeza para todas as turmas do turno")
						.operacao("SUB").valorMinimo(10).senso(limpeza).tipoRegra(tipoPorTurno)
						.roles(Arrays.asList(apoioAcademico, administrador)).build(),

				// Limpeza - Sistema - Positivas
				Regra.builder().descricao("20 pontos ao fim de cada bimestre se a turma tiver 100% de nota 10")
						.operacao("SUM").valorMinimo(20).senso(limpeza).tipoRegra(tipoAutomatico)
						.roles(Arrays.asList(sistema, administrador)).build(),

				// Limpeza - Administrador - Positivas
				Regra.builder().descricao("Até 20 pontos por campanha de coleta de lixo no campus").operacao("SUM")
						.valorMinimo(1).valorMaximo(20).senso(limpeza).tipoRegra(tipoVariavelBimestre)
						.roles(Arrays.asList(administrador)).build(),

				// Saúde - Docente - Positivas
				Regra.builder().descricao("0 pontos pela média menor ao do bimestre anterior").operacao("SUM")
						.grupo("media_comparativa").valorMinimo(0).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(docente, administrador)).build(),
				Regra.builder().descricao("8 pontos pela média igual ao do bimestre anterior").operacao("SUM")
						.grupo("media_comparativa").valorMinimo(8).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(docente, administrador)).build(),
				Regra.builder().descricao("20 pontos pela média maior ao do bimestre anterior").operacao("SUM")
						.grupo("media_comparativa").valorMinimo(20).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(docente, administrador)).build(),

				// Saúde - Assessoria Pedagogica - Positivas
				Regra.builder().descricao("0 pontos pela frequência menor ao do bimestre anterior").operacao("SUM")
						.grupo("frequencia_comparativa").valorMinimo(0).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(assessoriaPedagogica, administrador)).build(),
				Regra.builder().descricao("100 pontos pela frequência igual ao do bimestre anterior").operacao("SUM")
						.grupo("frequencia_comparativa").valorMinimo(100).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(assessoriaPedagogica, administrador)).build(),
				Regra.builder().descricao("200 pontos pela frequência maior ao do bimestre anterior").operacao("SUM")
						.grupo("frequencia_comparativa").valorMinimo(200).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(assessoriaPedagogica, administrador)).build(),

				// Saúde - Docente - Positivas
				Regra.builder().descricao("10 pontos para a turma que mais participou de CAs no bimestre (opcional)")
						.operacao("SUM").valorMinimo(10).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(docente, administrador)).build(),

				// Saúde - Docente - Negativas
				Regra.builder().descricao("0 a 15 pontos por bimestre por mau comportamento").operacao("SUB")
						.valorMinimo(0).valorMaximo(15).senso(saude).tipoRegra(tipoVariavelBimestre)
						.roles(Arrays.asList(docente, administrador)).build(),

				// Saúde - Coordenação de Curso - Positivas

				Regra.builder().descricao(
						"2 pontos por aluno de cada turma que participar de olimpíadas coordenadas pelo professor no bimestre extra")
						.operacao("SUM").valorMinimo(2).senso(saude).tipoRegra(tipoPorAlunoAno)
						.roles(Arrays.asList(coordenadorCurso, administrador)).build(),

				Regra.builder().descricao("1 ponto por aluno da turma em cada bimestre por atuação em monitoria")
						.operacao("SUM").valorMinimo(1).senso(saude).tipoRegra(tipoPorAlunoBimestre)
						.roles(Arrays.asList(coordenadorCurso, administrador)).build(),

				// Saúde - Assessoria Pedagógica - Positivas
				Regra.builder().descricao("1 ponto para a turma por aluno pela elaboração de plano de estudos")
						.operacao("SUM").valorMinimo(1).senso(saude).tipoRegra(tipoPorAlunoAno)
						.roles(Arrays.asList(assessoriaPedagogica, administrador)).build(),
				Regra.builder().descricao("10 pontos para a turma com maior participação nos Conselhos de Classe do bimestre")
						.operacao("SUM").valorMinimo(10).senso(saude).tipoRegra(tipoFixoPorBimestre)
						.roles(Arrays.asList(assessoriaPedagogica, administrador)).build(),
				Regra.builder().descricao(
						"40 pontos para a turma com presença dos pais maior que 75% nas reuniões de pais por reunião")
						.operacao("SUM").valorMinimo(40).senso(saude).tipoRegra(tipoFixo)
						.roles(Arrays.asList(assessoriaPedagogica, administrador)).build(),

				// Saúde - COEXPEIN - Positivas
				Regra.builder().descricao(
						"1 ponto por aluno da turma em cada bimestre por atuação em projetos de pesquisa/extensão")
						.operacao("SUM").valorMinimo(1).senso(saude).tipoRegra(tipoPorAlunoBimestre)
						.roles(Arrays.asList(coexpein, administrador)).build(),

				Regra.builder().descricao("10 a 140 pontos por premiação da turma na Expotec/Semadec do campus")
						.operacao("SUM").valorMinimo(10).valorMaximo(140).senso(saude).tipoRegra(tipoVariavel)
						.roles(Arrays.asList(coexpein, administrador)).build(),

				Regra.builder()
						.descricao(
								"2 pontos por participação do aluno da turma em eventos científicos externos ao campus")
						.operacao("SUM").valorMinimo(2).senso(saude).tipoRegra(tipoFixo)
						.roles(Arrays.asList(coexpein, administrador)).build(),

				// Autodisciplina - Apoio Acadêmico - Positivas
				Regra.builder().descricao("2 pontos por delação premiada").operacao("SUM").valorMinimo(2)
						.senso(autodisciplina).tipoRegra(tipoFixo)
						.roles(Arrays.asList(apoioAcademico, seac, administrador)).build(),

				// Autodisciplina - Apoio Acadêmico - Negativas
				Regra.builder().descricao("1 ponto por aluno da turma notificado").operacao("SUB").valorMinimo(1)
						.senso(autodisciplina).tipoRegra(tipoFixo)
						.roles(Arrays.asList(apoioAcademico, seac, administrador)).build(),
				Regra.builder().descricao("Pontos por turma notificada").operacao("SUB").senso(autodisciplina)
						.tipoRegra(tipoVariavel).roles(Arrays.asList(apoioAcademico, seac, administrador)).build(),
				Regra.builder().descricao("5 pontos por dia por aluno da turma suspenso").operacao("SUB").valorMinimo(1)
						.senso(autodisciplina).tipoRegra(tipoVariavel)
						.roles(Arrays.asList(apoioAcademico, seac, administrador)).build(),

				// Autodisciplina - Coordenador de Curso e Assessoria Pedagógica - Negativas
				Regra.builder().descricao("0 a 15 pontos por bimestre por campanha de conscientização realizada")
						.operacao("SUM").valorMinimo(0).valorMaximo(15).senso(autodisciplina)
						.tipoRegra(tipoVariavelBimestre)
						.roles(Arrays.asList(coordenadorCurso, assessoriaPedagogica, administrador)).build(),

				// Autodisciplina - Administrador - Negativas
				Regra.builder().descricao("5 a 10 pontos por má conduta em eventos").operacao("SUB").valorMinimo(5)
						.valorMaximo(10).senso(autodisciplina).tipoRegra(tipoVariavel)
						.roles(Arrays.asList(administrador)).build());

		regraRepository.saveAll(regras);

//		Criar pontuação
		Pontuacao pontuacao1 = Pontuacao.builder().contador(1).turma(turmaA).regra(regras.get(0)) // 1 ponto por livro
																									// emprestado
				.anoLetivo(ano23).user(u1).bimestre(1).pontos(1).motivacao("Livro emprestado").aplicado(true)
				.anulado(false).data(new Date(1673568000000L)) // Data de 13 de janeiro de 2023
				.build();

		Pontuacao pontuacao2 = Pontuacao.builder().contador(2).turma(turmaA).regra(regras.get(2)) // 30 pontos por
																									// campanha de
																									// doação
				.anoLetivo(ano23).user(u1).bimestre(1).pontos(30).motivacao("Campanha de doação").aplicado(false)
				.anulado(false).data(new Date(1678924800000L)) // Data de 15 de março de 2023
				.build();

		Pontuacao pontuacao3 = Pontuacao.builder().contador(3).turma(turmaA).regra(regras.get(7)) // 10 pontos por
																									// desordem no turno
				.anoLetivo(ano23).user(u1).bimestre(1).pontos(10).motivacao("Desordem no turno").aplicado(true)
				.anulado(false).data(new Date(1686787200000L)) // Data de 15 de junho de 2023
				.build();

		Pontuacao pontuacao4 = Pontuacao.builder().contador(4).turma(turmaA).regra(regras.get(10)) // 20 pontos por
																									// maior
																									// participação em
																									// CAs
				.anoLetivo(ano23).user(u1).bimestre(2).pontos(20).motivacao("Maior participação em CAs").aplicado(true)
				.anulado(false).data(new Date(1689379200000L)) // Data de 15 de julho de 2023
				.build();

		Pontuacao pontuacao5 = Pontuacao.builder().contador(5).turma(turmaA).regra(regras.get(12)) // 20 pontos para
																									// 100% de nota 10
				.anoLetivo(ano23).user(u1).bimestre(3).pontos(20).motivacao("100% de nota 10").aplicado(true)
				.anulado(false).data(new Date(1696118400000L)) // Data de 1 de outubro de 2023
				.build();

		Pontuacao pontuacao6 = Pontuacao.builder().contador(6).turma(turmaA).regra(regras.get(14)) // 10 pontos por
																									// limpeza excelente
				.anoLetivo(ano23).user(u1).bimestre(3).pontos(10).motivacao("Limpeza excelente").aplicado(true)
				.anulado(false).data(new Date(1698700800000L)) // Data de 30 de outubro de 2023
				.build();

		Pontuacao pontuacao7 = Pontuacao.builder().contador(7).turma(turmaA).regra(regras.get(15)) // 5 pontos por
																									// avaria de livro
				.anoLetivo(ano23).user(u1).bimestre(4).pontos(5).motivacao("Avaria de livro").aplicado(true)
				.anulado(false).data(new Date(1701302400000L)) // Data de 30 de novembro de 2023
				.build();

		Pontuacao pontuacao8 = Pontuacao.builder().contador(9).turma(turmaA).regra(regras.get(21)).anoLetivo(ano23)
				.user(u1).bimestre(4).pontos(5).motivacao("Avaria de livro").aplicado(true).anulado(false)
				.data(new Date(1701302400000L)).build();

		Pontuacao pontuacao9 = Pontuacao.builder().contador(8).turma(turmaA).regra(regras.get(36)).anoLetivo(ano23)
				.user(u1).bimestre(4).pontos(2).motivacao("Avaria de livro").aplicado(true).anulado(false)
				.data(new Date(1701302400000L)).build();

		Pontuacao pontuacao10 = Pontuacao.builder().contador(1).turma(turmaB).regra(regras.get(0)) // 1 ponto por livro
																									// emprestado
				.anoLetivo(ano23).user(u1).bimestre(1).pontos(1).motivacao("Livro emprestado").aplicado(true)
				.anulado(false).data(new Date(1673568000000L)) // Data de 13 de janeiro de 2023
				.build();

		Pontuacao pontuacao11 = Pontuacao.builder().contador(2).turma(turmaB).regra(regras.get(2)) // 30 pontos por
																									// campanha de
																									// doação
				.anoLetivo(ano23).user(u1).bimestre(1).pontos(7).motivacao("Campanha de doação").aplicado(false)
				.anulado(false).data(new Date(1678924800000L)) // Data de 15 de março de 2023
				.build();

		Pontuacao pontuacao12 = Pontuacao.builder().contador(3).turma(turmaB).regra(regras.get(7)) // 10 pontos por
																									// desordem no turno
				.anoLetivo(ano23).user(u1).bimestre(2).pontos(89).motivacao("Desordem no turno").aplicado(true)
				.anulado(false).data(new Date(1686787200000L)) // Data de 15 de junho de 2023
				.build();

		Pontuacao pontuacao13 = Pontuacao.builder().contador(4).turma(turmaB).regra(regras.get(10)) // 20 pontos por
																									// maior
																									// participação em
																									// CAs
				.anoLetivo(ano23).user(u1).bimestre(2).pontos(17).motivacao("Maior participação em CAs").aplicado(true)
				.anulado(false).data(new Date(1689379200000L)) // Data de 15 de julho de 2023
				.build();

		Pontuacao pontuacao14 = Pontuacao.builder().contador(5).turma(turmaB).regra(regras.get(12)) // 20 pontos para
																									// 100% de nota 10
				.anoLetivo(ano23).user(u1).bimestre(3).pontos(3).motivacao("100% de nota 10").aplicado(true)
				.anulado(false).data(new Date(1696118400000L)) // Data de 1 de outubro de 2023
				.build();

		Pontuacao pontuacao15 = Pontuacao.builder().contador(6).turma(turmaB).regra(regras.get(14)) // 10 pontos por
																									// limpeza excelente
				.anoLetivo(ano23).user(u1).bimestre(3).pontos(18).motivacao("Limpeza excelente").aplicado(true)
				.anulado(false).data(new Date(1698700800000L)) // Data de 30 de outubro de 2023
				.build();

		Pontuacao pontuacao16 = Pontuacao.builder().contador(9).turma(turmaB).regra(regras.get(15)) // 5 pontos por
																									// avaria de livro
				.anoLetivo(ano23).user(u1).bimestre(4).pontos(25).motivacao("Avaria de livro").aplicado(true)
				.anulado(false).data(new Date(1701302400000L)) // Data de 30 de novembro de 2023
				.build();

		Pontuacao pontuacao17 = Pontuacao.builder().contador(7).turma(turmaB).regra(regras.get(21)).anoLetivo(ano23)
				.user(u1).bimestre(4).pontos(15).motivacao("Avaria de livro").aplicado(true).anulado(false)
				.data(new Date(1701302400000L)).build();

		Pontuacao pontuacao18 = Pontuacao.builder().contador(8).turma(turmaB).regra(regras.get(36)).anoLetivo(ano23)
				.user(u1).bimestre(4).pontos(8).motivacao("Avaria de livro").aplicado(true).anulado(false)
				.data(new Date(1701302400000L)).build();

		// Salvar as pontuações
		pontuacaoRepository.saveAll(Arrays.asList(pontuacao1, pontuacao2, pontuacao3, pontuacao4, pontuacao5,
				pontuacao6, pontuacao7, pontuacao8, pontuacao9, pontuacao10, pontuacao11, pontuacao12, pontuacao13,
				pontuacao14, pontuacao15, pontuacao16, pontuacao17, pontuacao18));

	}

}
