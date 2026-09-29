package com.amorif;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.amorif.repository.RegraRepository;

@SpringBootTest
@ActiveProfiles("test")
class StartAppTests {

	@Autowired
	private RegraRepository regraRepository;

	@Test
	void contextLoadsWithCategorizedRules() {
		assertThat(regraRepository.findAll())
				.isNotEmpty()
				.allSatisfy(regra -> assertThat(regra.getCategoria()).isNotBlank());
		assertThat(regraRepository.findByAtivoTrue())
				.anySatisfy(regra -> assertThat(regra.getDescricao())
						.isEqualTo("2 pontos por aluno em cada olimpíada"));
		assertThat(regraRepository.findAll())
				.anySatisfy(regra -> {
					assertThat(regra.getDescricao())
							.isEqualTo("2 pontos por aluno participante de olimpíada");
					assertThat(regra.isAtivo()).isFalse();
				});
	}
}
