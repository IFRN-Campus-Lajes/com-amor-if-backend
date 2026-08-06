package com.amorif.config;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.amorif.entities.Regra;

/** Centraliza a redação e a categoria de apresentação das regras. */
public final class RegraCategorias {

    private record Definicao(String senso, String descricaoCanonica, String categoria, List<String> aliases) {
    }

    private static final List<Definicao> DEFINICOES = List.of(
            regra("Utilização", "1 ponto por livro emprestado", "Acervo e empréstimos",
                    "Pontos por livro emprestado (1 ponto por livro)", "1 ponto por livro emprestado pela turma"),
            regra("Utilização", "30 pontos por campanha de doação de livros", "Campanhas e grupos de estudo",
                    "30 pontos por campanha de doação"),
            regra("Utilização", "[TURBO] 20 pontos por bimestre para a turma com mais grupos de estudo",
                    "Campanhas e grupos de estudo",
                    "20 pontos no bimestre extra para a turma que mais tiver formado grupos de estudo no ano letivo",
                    "[TURBO] 20 pontos extras por bimestre para a turma que formar mais grupos de estudo"),
            regra("Utilização", "Perda de 20 pontos por livro não devolvido", "Ocorrências com o acervo",
                    "20 pontos por perda de livro"),
            regra("Utilização", "Perda de 3 pontos por livro devolvido com atraso", "Acervo e empréstimos",
                    "Pontos por atraso de livro (3 pontos por livro atrasado)",
                    "Subtração de 3 pontos por livro devolvido com atraso"),
            regra("Utilização", "Perda de 5 pontos por avaria em livro", "Ocorrências com o acervo",
                    "5 pontos por avaria de livro", "Subtração de 5 pontos por avaria de livro"),
            regra("Utilização", "Perda de X pontos por trapaça", "Ocorrências com o acervo", "Pontos por trapaça",
                    "Subtração de X pontos por trapaça"),

            regra("Ordenação", "0 pontos por organização ruim", "Avaliação da organização",
                    "0 pontos pela organização ruim", "0 pontos por avaliação ruim da organização da sala"),
            regra("Ordenação", "5 pontos por organização mediana", "Avaliação da organização",
                    "5 pontos pela organização mediana", "5 pontos por avaliação mediana da organização da sala"),
            regra("Ordenação", "8 pontos por organização boa", "Avaliação da organização",
                    "8 pontos pela organização boa", "8 pontos por avaliação boa da organização da sala"),
            regra("Ordenação", "10 pontos por organização excelente", "Avaliação da organização",
                    "10 pontos pela organização excelente", "10 pontos por avaliação excelente da organização da sala"),
            regra("Ordenação", "[TURBO] 20 pontos por 100% de avaliações excelentes no bimestre",
                    "Bônus de organização", "20 pontos ao fim de cada bimestre se a turma tiver 100% de nota 10",
                    "[TURBO] 20 pontos ao fim do bimestre para a turma com 100% das avaliações de organização excelentes"),
            regra("Ordenação", "Perda de 10 a 50 pontos por desordem ou dano intencional",
                    "Ocorrências de ordenação", "10 pontos por desordem para todas as turmas do turno",
                    "10 a 50 pontos por desordem ou dano intencional em sala ou laboratório",
                    "Subtração de 10 a 50 pontos por desordem ou quebra intencional de equipamentos em laboratórios e salas de aula"),

            regra("Limpeza", "0 pontos por limpeza ruim", "Avaliação da limpeza", "0 pontos pela limpeza ruim",
                    "0 pontos por avaliação ruim da limpeza da sala"),
            regra("Limpeza", "5 pontos por limpeza mediana", "Avaliação da limpeza", "5 pontos pela limpeza mediana",
                    "5 pontos por avaliação mediana da limpeza da sala"),
            regra("Limpeza", "8 pontos por limpeza boa", "Avaliação da limpeza", "8 pontos pela limpeza boa",
                    "8 pontos por avaliação boa da limpeza da sala"),
            regra("Limpeza", "10 pontos por limpeza excelente", "Avaliação da limpeza",
                    "10 pontos pela limpeza excelente", "10 pontos por avaliação excelente da limpeza da sala"),
            regra("Limpeza", "Perda de 10 pontos por falta de limpeza nas turmas do turno", "Ocorrências de limpeza",
                    "10 pontos por falta de limpeza para todas as turmas do turno",
                    "Subtração de 10 pontos por falta de limpeza em todas as turmas do turno"),
            regra("Limpeza", "[TURBO] 20 pontos por 100% de avaliações excelentes no bimestre",
                    "Bônus de limpeza", "20 pontos ao fim de cada bimestre se a turma tiver 100% de nota 10",
                    "[TURBO] 20 pontos ao fim do bimestre para a turma com 100% das avaliações de limpeza excelentes"),
            regra("Limpeza", "Até 20 pontos por campanha de coleta de lixo", "Campanhas ambientais",
                    "Até 20 pontos por campanha de coleta de lixo no campus"),

            regra("Saúde", "0 pontos se a média diminuir em relação ao bimestre anterior",
                    "Desempenho acadêmico", "0 pontos pela média menor ao do bimestre anterior",
                    "0 pontos caso a média de notas da turma seja inferior à do bimestre anterior"),
            regra("Saúde", "8 pontos se a média se mantiver ou cair até 0,3 ponto",
                    "Desempenho acadêmico", "8 pontos pela média igual ao do bimestre anterior",
                    "8 pontos caso a média de notas da turma se mantenha igual ou diminua até 0,3 ponto em relação ao bimestre anterior"),
            regra("Saúde", "20 pontos se a média aumentar em relação ao bimestre anterior",
                    "Desempenho acadêmico", "20 pontos pela média maior ao do bimestre anterior",
                    "20 pontos caso a média de notas da turma aumente em relação ao bimestre anterior"),
            regra("Saúde", "0 pontos se a frequência diminuir em relação ao bimestre anterior",
                    "Frequência escolar", "0 pontos pela frequência menor ao do bimestre anterior",
                    "0 pontos caso a média de frequência da turma seja inferior à do bimestre anterior"),
            regra("Saúde", "100 pontos se a frequência se mantiver ou cair até 2 pontos percentuais",
                    "Frequência escolar", "100 pontos pela frequência igual ao do bimestre anterior",
                    "100 pontos caso a frequência da turma se mantenha igual ou diminua até 2 pontos percentuais em relação ao bimestre anterior"),
            regra("Saúde", "200 pontos, caso a frequência tenha aumentado ou se mantido no máximo",
                    "Frequência escolar", "200 pontos pela frequência maior ao do bimestre anterior"),
            regra("Saúde", "10 pontos para a turma com maior participação em CAs no bimestre",
                    "Desempenho acadêmico", "10 pontos para a turma que mais participou de CAs no bimestre (opcional)",
                    "10 pontos para a turma com maior participação em CAs no bimestre (opcional)"),
            regra("Saúde", "Perda de 0 a 15 pontos por mau comportamento no bimestre", "Convivência e comportamento",
                    "0 a 15 pontos por bimestre por mau comportamento",
                    "Subtração de 0 a 15 pontos por bimestre por mau comportamento, como ausência coletiva ou atrasos"),
            regra("Saúde", "2 pontos por aluno participante de olimpíada", "Desempenho acadêmico",
                    "2 pontos por aluno de cada turma que participar de olimpíadas coordenadas pelo professor no bimestre extra",
                    "2 pontos por aluno da turma que participar de olimpíadas coordenadas por docente"),
            regra("Saúde", "1 ponto por aluno monitor no bimestre", "Desempenho acadêmico",
                    "1 ponto por aluno da turma em cada bimestre por atuação em monitoria",
                    "1 ponto por aluno da turma por atuação em monitoria, a cada bimestre"),
            regra("Saúde", "1 ponto por aluno com plano de estudos", "Desempenho acadêmico",
                    "1 ponto para a turma por aluno pela elaboração de plano de estudos",
                    "1 ponto por aluno da turma pela elaboração de plano de estudos, a cada ano letivo"),
            regra("Saúde", "40 pontos por mais de 75% de responsáveis presentes na reunião",
                    "Família e acompanhamento",
                    "40 pontos para a turma com presença dos pais maior que 75% nas reuniões de pais por reunião",
                    "40 pontos por reunião para a turma com presença de responsáveis superior a 75%"),
            regra("Saúde", "10 pontos para a turma com maior participação no Conselho de Classe",
                    "Desempenho acadêmico",
                    "10 pontos para a turma com maior participação nos Conselhos de Classe do bimestre",
                    "10 pontos para a turma com maior participação no Conselho de Classe do bimestre"),
            regra("Saúde", "1 ponto por aluno em projeto de pesquisa ou extensão no bimestre",
                    "Pesquisa, extensão e eventos",
                    "1 ponto por aluno da turma em cada bimestre por atuação em projetos de pesquisa/extensão",
                    "1 ponto por aluno da turma por atuação em projeto de pesquisa ou extensão, a cada bimestre"),
            regra("Saúde", "10 a 140 pontos para as turmas com maior pontuação em eventos do campus",
                    "Pesquisa, extensão e eventos", "10 a 140 pontos por premiação da turma na Expotec/Semadec do campus",
                    "10 a 140 pontos para as turmas com maior pontuação acumulada nos eventos do campus"),
            regra("Saúde", "2 pontos por aluno em evento científico externo", "Pesquisa, extensão e eventos",
                    "2 pontos por participação do aluno da turma em eventos científicos externos ao campus",
                    "2 pontos por participação de aluno da turma em evento científico externo ao campus"),
            regra("Saúde", "2 pontos por aluno em avaliação biomédica ou odontológica",
                    "Saúde e assistência estudantil", "1 ponto por aluno da turma que realizar Avaliação Biomédica de Saúde",
                    "2 pontos por aluno da turma que realizar avaliação biomédica ou odontológica"),
            regra("Saúde", "1 ponto por aluno com caracterização socioeconômica",
                    "Saúde e assistência estudantil",
                    "1 ponto por aluno da turma que realizar Caracterização Socioeconômica",
                    "1 ponto por aluno da turma que realizar caracterização socioeconômica"),
            regra("Saúde", "15 a 45 pontos por campanha educativa dos alunos", "Campanhas educativas",
                    "15 a 45 Pontos por Campanhas Educativas Organizadas por Alunos e Autorizadas por Setores Administrativos ou Pedagógicos",
                    "15 a 45 pontos por campanha educativa organizada por alunos e autorizada por setor administrativo ou pedagógico"),
            regra("Saúde", "[TURBO] 50 a 300 pontos por divulgação dos processos seletivos",
                    "Campanhas educativas", "[TURBO] 15 a 100 Pontos por Campanhas Educativas",
                    "[TURBO] 50 a 300 pontos por campanha de divulgação dos processos seletivos do IFRN em redes sociais ou presencialmente"),
            regra("Saúde", "5 pontos por aluno atuante em núcleo do campus no bimestre",
                    "Participação estudantil",
                    "5 pontos por aluno da turma por atuação em núcleo do campus, a cada bimestre"),

            regra("Autodisciplina", "2 pontos por delação premiada", "Reconhecimento"),
            regra("Autodisciplina", "Perda de 1 ponto por aluno notificado", "Ocorrências disciplinares",
                    "1 ponto por aluno da turma notificado", "Subtração de 1 ponto por aluno da turma notificado"),
            regra("Autodisciplina", "Perda de X pontos por turma notificada", "Ocorrências disciplinares",
                    "Pontos por turma notificada", "Subtração de X pontos por turma notificada"),
            regra("Autodisciplina", "Perda de 5 pontos por dia de suspensão do aluno", "Ocorrências disciplinares",
                    "5 pontos por dia por aluno da turma suspenso",
                    "Subtração de 5 pontos por dia de suspensão de aluno da turma"),
            regra("Autodisciplina", "0 a 15 pontos por campanha de conscientização no bimestre",
                    "Campanhas de conscientização",
                    "0 a 15 pontos por bimestre por campanha de conscientização realizada",
                    "0 a 15 pontos por campanha de conscientização realizada no bimestre"),
            regra("Autodisciplina", "Perda de 5 a 10 pontos por má conduta em evento", "Conduta em eventos",
                    "5 a 10 pontos por má conduta em eventos", "Subtração de 5 a 10 pontos por má conduta em evento"));

    private RegraCategorias() {
    }

    public static void aplicar(List<Regra> regras) {
        for (Regra regra : regras) {
            Definicao definicao = localizar(regra);
            if (definicao == null) {
                throw new IllegalArgumentException("Regra sem definição no catálogo: "
                        + regra.getSenso().getDescricao() + " | " + regra.getDescricao());
            }
            regra.setDescricao(definicao.descricaoCanonica());
            regra.setCategoria(definicao.categoria());
        }
    }

    public static boolean corresponde(Regra existente, Regra canonica) {
        Definicao definicao = localizar(canonica);
        if (definicao == null || existente.getSenso() == null) {
            return false;
        }
        return normalizar(definicao.senso()).equals(normalizar(existente.getSenso().getDescricao()))
                && definicao.aliases().stream().anyMatch(alias -> normalizar(alias).equals(normalizar(existente.getDescricao())));
    }

    public static Set<String> descricoesCanonicas() {
        return DEFINICOES.stream().map(Definicao::descricaoCanonica).collect(Collectors.toUnmodifiableSet());
    }

    private static Definicao localizar(Regra regra) {
        String senso = normalizar(regra.getSenso().getDescricao());
        String descricao = normalizar(regra.getDescricao());
        return DEFINICOES.stream()
                .filter(item -> normalizar(item.senso()).equals(senso))
                .filter(item -> item.aliases().stream().anyMatch(alias -> normalizar(alias).equals(descricao)))
                .findFirst().orElse(null);
    }

    private static Definicao regra(String senso, String canonica, String categoria, String... aliases) {
        List<String> descricoes = new java.util.ArrayList<>(Arrays.asList(aliases));
        descricoes.add(canonica);
        return new Definicao(senso, canonica, categoria, List.copyOf(descricoes));
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}
