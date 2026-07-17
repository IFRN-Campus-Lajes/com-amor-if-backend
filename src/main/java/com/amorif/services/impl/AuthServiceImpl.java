package com.amorif.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.amorif.config.security.JWTTokenProvider;
import com.amorif.dto.request.RegisterDtoRequest;
import com.amorif.dto.request.SUAPUserDtoRequest;
import com.amorif.dto.response.AuthenticationDtoResponse;
import com.amorif.entities.Role;
import com.amorif.entities.RoleEnum;
import com.amorif.entities.Token;
import com.amorif.entities.User;
import com.amorif.exceptions.InvalidJWTAuthenticationException;
import com.amorif.repository.RoleRepository;
import com.amorif.repository.TokenRepository;
import com.amorif.repository.UserRepository;
import com.amorif.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author osailton
 */

@Service
public class AuthServiceImpl implements AuthService {

	@Value("${SUAP_USER_INFO_URL}")
	private String SUAP_USER_INFO_URL;

	@Value("${SUAP_TOKEN_URL}")
	private String SUAP_TOKEN_URL;

	@Value("${SUAP_CLIENT_ID}")
	private String SUAP_CLIENT_ID;

	@Value("${SUAP_CLIENT_SECRET}")
	private String SUAP_CLIENT_SECRET;

	@Value("${SUAP_REDIRECT_URI}")
	private String SUAP_REDIRECT_URI;

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TokenRepository tokenRepository;
	private final JWTTokenProvider jwtTokenProvider;

	public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			TokenRepository tokenRepository, JWTTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.tokenRepository = tokenRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Override
	public AuthenticationDtoResponse getUserFromToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if(header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		String token = header.substring(7);
		String matricula = request.getUserPrincipal().getName();
		if(matricula == null) {
			return null;
		}
		User user = userRepository.findByMatricula(matricula).orElseThrow();
		if(!jwtTokenProvider.validateToken(token)) {
			return null;
		}
		String refToken = jwtTokenProvider.getStringRefreshToken(user.getMatricula(),
				user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList()));
		return AuthenticationDtoResponse.builder().accessToken(token).refreshToken(refToken)
				.matricula(user.getMatricula()).nome(user.getNome())
				.roles(user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList())).build();
	}

	@Override
	public AuthenticationDtoResponse register(SUAPUserDtoRequest dto) {
		User user = User.builder().matricula(dto.getMatricula()).nome(dto.getNomeUsual()).build();
		user.setEmail(dto.getEmail());

		assignRoles(user, dto);

		this.userRepository.save(user);

		// Generate token
		String token = jwtTokenProvider.getStringAccessToken(user.getMatricula(),
				user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList()));
		String refToken = jwtTokenProvider.getStringRefreshToken(user.getMatricula(),
				user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList()));
		saveUserToken(user, token);

		return AuthenticationDtoResponse.builder().accessToken(token).refreshToken(refToken)
				.matricula(user.getMatricula()).nome(user.getNome())
				.roles(user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList())).build();
	}

	@Override
	public RegisterDtoRequest getTokenFromCode(String code) {

//		Get the suap token from the code
		CloseableHttpClient httpclient = HttpClients.createDefault();
		RegisterDtoRequest dto = null;

		try {
			HttpPost post = new HttpPost(this.SUAP_TOKEN_URL);
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			post.addHeader("Accept", "application/json");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("client_id", this.SUAP_CLIENT_ID));
			params.add(new BasicNameValuePair("client_secret", this.SUAP_CLIENT_SECRET));
			params.add(new BasicNameValuePair("code", code));
			params.add(new BasicNameValuePair("grant_type", "authorization_code"));
			post.setEntity(new UrlEncodedFormEntity(params));

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						HttpEntity entity = response.getEntity();
			            String errorBody = entity != null ? EntityUtils.toString(entity) : "No content";
			            System.err.println("Error Response Body: " + errorBody);
						throw new InvalidJWTAuthenticationException("Credenciais inválidas!");
					}
				}
			};

			String response = httpclient.execute(post, responseHandler);

			ObjectMapper mapper = new ObjectMapper();
			dto = mapper.readValue(response, RegisterDtoRequest.class);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dto;
	}

	@Override
	public SUAPUserDtoRequest getSuapUser(String token) {

//		Get the user info from the token
		CloseableHttpClient httpclient = HttpClients.createDefault();
		SUAPUserDtoRequest dto = null;

		try {
			HttpGet get = new HttpGet(SUAP_USER_INFO_URL);
			get.addHeader("Accept", "application/json");
			get.addHeader("Authorization", "Bearer " + token);

			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new InvalidJWTAuthenticationException("Credenciais inválidas!");
					}
				}
			};
			String response = httpclient.execute(get, responseHandler);
			ObjectMapper mapper = new ObjectMapper();
			dto = mapper.readValue(response, SUAPUserDtoRequest.class);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return dto;
	}

	@Override
	public boolean existsByMatricula(String matricula) {
		return !this.userRepository.findByMatricula(matricula).isEmpty();
	}

	@Override
	public AuthenticationDtoResponse authenticate(SUAPUserDtoRequest dto) {
		User user = this.userRepository.findByMatricula(dto.getMatricula()).orElseThrow();
		
		assignRoles(user, dto); // Update the User Roles
		userRepository.save(user);
		
		String token = jwtTokenProvider.getStringAccessToken(user.getMatricula(),
				user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList()));
		String refToken = jwtTokenProvider.getStringRefreshToken(user.getMatricula(),
				user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList()));

//		Revoke all other tokens
		revokeAllUserTokens(user);
		saveUserToken(user, token);

		return AuthenticationDtoResponse.builder().accessToken(token).refreshToken(refToken)
				.matricula(user.getMatricula()).nome(user.getNome())
				.roles(user.getFuncoes().stream().map(Role::getName).collect(Collectors.toList())).build();
	}

	private void saveUserToken(User user, String jwtToken) {
		Token token = Token.builder().user(user).token(jwtToken).tokenType("BEARER").expired(false).revoked(false)
				.build();
		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user.getId());
		if (validTokens.isEmpty()) {
			return;
		}
		for (Token token : validTokens) {
			token.setExpired(true);
			token.setRevoked(true);
		}
		tokenRepository.saveAll(validTokens);
	}
	
	private void assignRoles(User user, SUAPUserDtoRequest dto) {
		user.getFuncoes().clear();
		
		// Definindo as roles para setorSuap
	    Map<String, String> setorSuapRoles = new HashMap<>();
	    setorSuapRoles.put("ASBIB/LAJ", RoleEnum.ROLE_BIBLIOTECARIO.toString());
	    setorSuapRoles.put("COADESC/LAJ", RoleEnum.ROLE_APOIO_ACADEMICO.toString());
	    setorSuapRoles.put("ASPED/LAJ", RoleEnum.ROLE_ASSESSORIA_PEDAGOGICA.toString());
	    setorSuapRoles.put("COAES/LAJ", RoleEnum.ROLE_ASSISTENCIA_ESTUDANTIL.toString());
	    setorSuapRoles.put("COLAB/LAJ", RoleEnum.ROLE_ASSESSORIA_LABORATORIO.toString());
	    setorSuapRoles.put("COCINF/LAJ", RoleEnum.ROLE_COORDENADOR_CURSO.toString());
	    setorSuapRoles.put("COCADM/LAJ", RoleEnum.ROLE_COORDENADOR_CURSO.toString());
	    setorSuapRoles.put("COCOM/LAJ", RoleEnum.ROLE_COORDENADOR_CURSO.toString());
	    setorSuapRoles.put("CRA/LAJ", RoleEnum.ROLE_SEAC.toString());
	    setorSuapRoles.put("COPEIN/LAJ", RoleEnum.ROLE_COEXPEIN.toString());
		setorSuapRoles.put("COEX/LAJ", RoleEnum.ROLE_COEXPEIN.toString());
	    setorSuapRoles.put("DIAD/LAJ", RoleEnum.ROLE_DIAD.toString());

	    // Funções específicas para ADMINISTRADOR
	    List<String> adminFunctions = Arrays.asList(
	        "FG0002 - COTIC/LAJ", "CD2 - DG/LAJ", "SUB-CHEFIA - DG/LAJ", "CD0004 - DIAC/LAJ"
	    );

	    // Condições para atribuição de roles
	    if (dto.getTipoVinculo().contains("Servidor") && !dto.getVinculo().getCategoria().contains("estagiario")) {
	        // Adicionando roles com base no setorSuap
	        for (Map.Entry<String, String> entry : setorSuapRoles.entrySet()) {
	            if (dto.getVinculo().getSetorSuap().contains(entry.getKey())) {
	                user.getFuncoes().add(this.roleRepository.getByName(entry.getValue()));
	            }
	        }

	        // Verificando se a função está na lista de administradores
	        if (adminFunctions.stream().anyMatch(funcao -> Arrays.asList(dto.getVinculo().getFuncao()).contains(funcao))) {
	            user.getFuncoes().add(this.roleRepository.getByName(RoleEnum.ROLE_ADMINISTRADOR.toString()));
	        }

	        // Verificando a categoria para adicionar ROLE_DOCENTE
	        if (dto.getVinculo().getCategoria().contains("docente")) {
	            user.getFuncoes().add(this.roleRepository.getByName(RoleEnum.ROLE_DOCENTE.toString()));
	        }
	    } else {	    	
	    	// Adiciona ROLE_ALUNO por padrão se não for servidor
	    	user.getFuncoes().add(this.roleRepository.getByName(RoleEnum.ROLE_ALUNO.toString()));
	    }
	}

}
