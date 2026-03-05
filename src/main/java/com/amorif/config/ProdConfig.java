package com.amorif.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amorif.entities.FrequenciaRegraEnum;
import com.amorif.entities.Role;
import com.amorif.entities.RoleEnum;
import com.amorif.entities.Senso;
import com.amorif.entities.Regra;
import com.amorif.entities.TipoRegra;
import com.amorif.repository.RoleRepository;
import com.amorif.repository.SensoRepository;
import com.amorif.repository.TipoRegraRepository;
import com.amorif.repository.RegraRepository;

/**
 * @author manoel
 */

@Configuration
@Profile("prod")
public class ProdConfig implements CommandLineRunner {

	private final RoleRepository roleRepository;
	private final SensoRepository sensoRepository;
	private final TipoRegraRepository tipoRegraRepository;
	private final RegraRepository regraRepository;

	public ProdConfig(RoleRepository roleRepository, SensoRepository sensoRepository,
			TipoRegraRepository tipoRegraRepository, RegraRepository regraRepository) {
		this.roleRepository = roleRepository;
		this.sensoRepository = sensoRepository;
		this.tipoRegraRepository = tipoRegraRepository;
		this.regraRepository = regraRepository;
	}

	@Override
	public void run(String... args) throws Exception {
//		// Create Roles
		List<String> rolesExistentes = roleRepository.findAll().stream().map(Role::getName)
				.collect(Collectors.toList());

		List<Role> novasRoles = Arrays.asList(Role.builder().name(RoleEnum.ROLE_ADMINISTRADOR.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_AVAL.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_ALUNO.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_BIBLIOTECARIO.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_APOIO_ACADEMICO.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_SISTEMA.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_DOCENTE.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_ASSESSORIA_PEDAGOGICA.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_COEXPEIN.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_COORDENADOR_CURSO.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_ASSESSORIA_LABORATORIO.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_ASSISTENCIA_ESTUDANTIL.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_SEAC.toString()).build(),
				Role.builder().name(RoleEnum.ROLE_DIAD.toString()).build());

		List<Role> rolesParaSalvar = novasRoles.stream().filter(role -> !rolesExistentes.contains(role.getName()))
				.collect(Collectors.toList());

		if (!rolesParaSalvar.isEmpty()) {
			roleRepository.saveAll(rolesParaSalvar);
		}

		// Criar sensos
		List<String> sensosExistentes = sensoRepository.findAll().stream().map(Senso::getDescricao)
				.collect(Collectors.toList());

		List<Senso> novosSensos = Arrays.asList(Senso.builder().descricao("Utilização").build(),
				Senso.builder().descricao("Ordenação").build(), Senso.builder().descricao("Limpeza").build(),
				Senso.builder().descricao("Saúde").build(), Senso.builder().descricao("Autodisciplina").build());

		// Filtra apenas os sensos que ainda não existem no banco
		List<Senso> sensosParaSalvar = novosSensos.stream()
				.filter(senso -> !sensosExistentes.contains(senso.getDescricao())).collect(Collectors.toList());

		if (!sensosParaSalvar.isEmpty()) {
			sensoRepository.saveAll(sensosParaSalvar);
		}

		// Criar tipos de regra
		List<String> tiposExistentes = tipoRegraRepository.findAll().stream().map(TipoRegra::getDescricao)
				.collect(Collectors.toList());

		List<TipoRegra> novosTipos = Arrays.asList(
				TipoRegra.builder().descricao("Valor Fixo").fixo(true).temAluno(false)
						.frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).build(),
				TipoRegra.builder().descricao("Valor Fixo em bimestre extra por ano letivo").fixo(true).temAluno(false)
						.frequencia(FrequenciaRegraEnum.ANUAL.ordinal()).bimestreExtra(true).build(),
				TipoRegra.builder().descricao("Valor Fixo automático por bimestre").fixo(true).temAluno(false)
						.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).automatico(true).build(),
				TipoRegra.builder().descricao("Valor Fixo por turno").fixo(true).temAluno(false)
						.frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).porTurno(true).build(),
				TipoRegra.builder().descricao("Valor Fixo por aluno por bimestre").fixo(true).temAluno(true)
						.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build(),
				TipoRegra.builder().descricao("Valor Variável por bimestre").fixo(false).temAluno(false)
						.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build(),
				TipoRegra.builder().descricao("Valor Fixo por aluno por ano letivo").fixo(true).temAluno(true)
						.frequencia(FrequenciaRegraEnum.ANUAL.ordinal()).build(),
				TipoRegra.builder().descricao("Valor Variável").fixo(false).temAluno(false)
						.frequencia(FrequenciaRegraEnum.AVULSO.ordinal()).build(),
				TipoRegra.builder().descricao("Valor Fixo por bimestre").fixo(true).temAluno(false)
						.frequencia(FrequenciaRegraEnum.BIMESTRAL.ordinal()).build());

		List<TipoRegra> tiposParaSalvar = novosTipos.stream()
				.filter(tipo -> !tiposExistentes.contains(tipo.getDescricao())).collect(Collectors.toList());

		if (!tiposParaSalvar.isEmpty()) {
			tipoRegraRepository.saveAll(tiposParaSalvar);
		}

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
		Role diad = roleRepository.getByName("ROLE_DIAD");

		// População das regras
		List<String> regrasExistentes = regraRepository.findAll().stream().map(Regra::getDescricao)
				.collect(Collectors.toList());

		List<Regra> novasRegras = Arrays.asList(
				// Utilização - Bibliotecário - Positivas
				Regra.builder().descricao("Pontos por livro emprestado (1 ponto por livro)").operacao("SUM")
						.valorMinimo(1).senso(utilizacao).tipoRegra(tipoVariavel)
						.roles(Arrays.asList(bibliotecario, administrador)).build(),
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
				Regra.builder().descricao("Pontos por atraso de livro (3 pontos por livro atrasado)").operacao("SUB")
						.valorMinimo(3).senso(utilizacao).tipoRegra(tipoVariavel)
						.roles(Arrays.asList(bibliotecario, administrador)).build(),
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

				Regra.builder().descricao(
						"40 pontos para a turma com presença dos pais maior que 75% nas reuniões de pais por reunião")
						.operacao("SUM").valorMinimo(40).senso(saude).tipoRegra(tipoFixo)
						.roles(Arrays.asList(assessoriaPedagogica, administrador)).build(),

				Regra.builder()
						.descricao("10 pontos para a turma com maior participação nos Conselhos de Classe do bimestre")
						.operacao("SUM").valorMinimo(10).senso(saude).tipoRegra(tipoFixoPorBimestre)
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

				// Saúde - ASAES - Positivas

				Regra.builder().descricao("1 ponto por aluno da turma que realizar Avaliação Biomédica de Saúde")
						.operacao("SUM").valorMinimo(1).senso(saude).tipoRegra(tipoPorAlunoAno)
						.roles(Arrays.asList(assistenciaEstudantil, administrador)).build(),

				Regra.builder().descricao("1 ponto por aluno da turma que realizar Caracterização Socioeconômica")
						.operacao("SUM").valorMinimo(1).senso(saude).tipoRegra(tipoPorAlunoAno)
						.roles(Arrays.asList(assistenciaEstudantil, administrador)).build(),

				// Saúde - DG, DIAD, DIAC - Positivas

				Regra.builder().descricao("15 a 45 Pontos por Campanhas Educativas Organizadas por Alunos e Autorizadas por Setores Administrativos ou Pedagógicos\r\n"
						+ "")
						.operacao("SUM").valorMinimo(15).valorMaximo(45).senso(saude).tipoRegra(tipoVariavel)
						.roles(Arrays.asList(administrador, diad)).build(),

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

		List<Regra> regrasParaSalvar = novasRegras.stream()
				.filter(regra -> !regrasExistentes.contains(regra.getDescricao())).collect(Collectors.toList());

		if (!regrasParaSalvar.isEmpty()) {
			regraRepository.saveAll(regrasParaSalvar);
		}

	}

}
