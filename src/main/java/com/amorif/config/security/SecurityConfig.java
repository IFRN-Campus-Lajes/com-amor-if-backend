package com.amorif.config.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.amorif.entities.RoleEnum;

/**
 * @author osailton
 */

@Configuration
@EnableWebSecurity
@Profile({"dev", "prod"})
public class SecurityConfig {

	private final String[] WHITE_LIST = new String[] { "/api/auth/**", "/api/public/**", "/api/pontuacao/pontosDoAnoCorrente" };

	private final String[] AVAL_LIST = new String[] { "/api/pontuacao/**", "/api/anoletivo/**", "/api/turma/**", "/api/regras/**", "/api/olimpiadas/**" };

	private final String[] MANAGER_LIST = new String[] { "/api/manager/**" };

	private final AuthenticationProvider authenticationProvider;
	private final JWTAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(AuthenticationProvider authenticationProvider,
			JWTAuthenticationFilter jwtAuthenticationFilter) {
		this.authenticationProvider = authenticationProvider;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, CustomAuthenticationEntryPoint authenticationEntryPoint) throws Exception {
		return httpSecurity.httpBasic((basic) -> basic.disable()).csrf((csrf) -> csrf.disable())
				.cors(httpSecurityCorsConfigurer  -> {
					CorsConfiguration config = new CorsConfiguration();
					config.setAllowedOrigins(Arrays.asList("*"));
					config.setAllowedMethods(Arrays.asList("*"));
					config.setAllowedHeaders(Arrays.asList("*"));
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", config);
                    httpSecurityCorsConfigurer.configurationSource(source);
				})
				.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests((auth) -> auth
						.requestMatchers(WHITE_LIST).permitAll()
						.requestMatchers("/api/pontuacao/lancarPontosAutomaticos")
						.hasRole(this.getRole(RoleEnum.ROLE_ADMINISTRADOR.toString()))
						.requestMatchers("/api/anoletivo/ano")
						.hasRole(this.getRole(RoleEnum.ROLE_ADMINISTRADOR.toString()))
						.requestMatchers("/api/turma/turma")
						.hasRole(this.getRole(RoleEnum.ROLE_ADMINISTRADOR.toString()))
						.requestMatchers(HttpMethod.POST, "/api/olimpiadas/**")
						.hasAnyRole(this.getRole(RoleEnum.ROLE_ADMINISTRADOR.toString()),
								this.getRole(RoleEnum.ROLE_COORDENADOR_CURSO.toString()))
						.requestMatchers(HttpMethod.PUT, "/api/olimpiadas/**")
						.hasAnyRole(this.getRole(RoleEnum.ROLE_ADMINISTRADOR.toString()),
								this.getRole(RoleEnum.ROLE_COORDENADOR_CURSO.toString()))
						.requestMatchers(HttpMethod.DELETE, "/api/olimpiadas/**")
						.hasAnyRole(this.getRole(RoleEnum.ROLE_ADMINISTRADOR.toString()),
								this.getRole(RoleEnum.ROLE_COORDENADOR_CURSO.toString()))
						.requestMatchers(AVAL_LIST).hasAnyRole(Stream.concat(
					            getRolesByCategory("Aval").stream(), // First list
					            getRolesByCategory("Admin").stream() // Second list
					        ).map(role -> getRole(role))
					        .toArray(String[]::new))
						.requestMatchers(MANAGER_LIST).hasAnyRole(
								this.getRole(RoleEnum.ROLE_ADMINISTRADOR.toString()))
						.requestMatchers("/users").denyAll().anyRequest().authenticated())
				.exceptionHandling(exceptionHandling -> exceptionHandling
	                    .authenticationEntryPoint(authenticationEntryPoint) // Adiciona tratamento de erro 401
	            )
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authenticationProvider(this.authenticationProvider).build();
	}

	private String getRole(String role) {
		return role.substring("ROLE_".length());
	}
	
	private List<String> getRolesByCategory(String category) {
        return RoleEnum.getByCategory(category).stream()
                .map(role -> role.toString()) // Convert enum to its string representation
                .collect(Collectors.toList());
    }
}
