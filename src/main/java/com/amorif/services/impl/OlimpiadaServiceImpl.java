package com.amorif.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amorif.dto.request.OlimpiadaDtoRequest;
import com.amorif.dto.response.OlimpiadaDtoResponse;
import com.amorif.entities.Olimpiada;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.repository.OlimpiadaRepository;
import com.amorif.repository.PontuacaoRepository;
import com.amorif.services.OlimpiadaService;

@Service
public class OlimpiadaServiceImpl implements OlimpiadaService {

	private final OlimpiadaRepository olimpiadaRepository;
	private final PontuacaoRepository pontuacaoRepository;

	public OlimpiadaServiceImpl(OlimpiadaRepository olimpiadaRepository, PontuacaoRepository pontuacaoRepository) {
		this.olimpiadaRepository = olimpiadaRepository;
		this.pontuacaoRepository = pontuacaoRepository;
	}

	@Override
	public List<OlimpiadaDtoResponse> listAll() {
		return olimpiadaRepository.findAllByOrderByNomeAsc().stream()
				.map(OlimpiadaDtoResponse::fromOlimpiada).toList();
	}

	@Override
	@Transactional
	public OlimpiadaDtoResponse create(OlimpiadaDtoRequest request) {
		String nome = validName(request);
		if (olimpiadaRepository.existsByNomeIgnoreCase(nome)) {
			throw new InvalidArgumentException("Já existe uma olimpíada com esse nome.");
		}
		return OlimpiadaDtoResponse.fromOlimpiada(olimpiadaRepository.save(Olimpiada.builder().nome(nome).build()));
	}

	@Override
	@Transactional
	public OlimpiadaDtoResponse update(Long id, OlimpiadaDtoRequest request) {
		Olimpiada olimpiada = olimpiadaRepository.findById(id)
				.orElseThrow(() -> new InvalidArgumentException("Olimpíada não encontrada."));
		String nome = validName(request);
		if (olimpiadaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
			throw new InvalidArgumentException("Já existe uma olimpíada com esse nome.");
		}
		olimpiada.setNome(nome);
		return OlimpiadaDtoResponse.fromOlimpiada(olimpiadaRepository.save(olimpiada));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		if (!olimpiadaRepository.existsById(id)) {
			throw new InvalidArgumentException("Olimpíada não encontrada.");
		}
		if (pontuacaoRepository.existsByOlimpiadaId(id)) {
			throw new InvalidArgumentException("Não é possível excluir uma olimpíada vinculada a pontuações.");
		}
		olimpiadaRepository.deleteById(id);
	}

	private String validName(OlimpiadaDtoRequest request) {
		String nome = request == null || request.getNome() == null ? "" : request.getNome().trim();
		if (nome.isEmpty() || nome.length() > 150) {
			throw new InvalidArgumentException("Informe um nome de olimpíada com até 150 caracteres.");
		}
		return nome;
	}
}
