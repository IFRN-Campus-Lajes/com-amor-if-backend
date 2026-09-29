package com.amorif.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amorif.dto.request.OlimpiadaDtoRequest;
import com.amorif.dto.response.OlimpiadaDtoResponse;
import com.amorif.services.OlimpiadaService;

@RestController
@RequestMapping("/api/olimpiadas")
public class OlimpiadaController {

	private final OlimpiadaService olimpiadaService;

	public OlimpiadaController(OlimpiadaService olimpiadaService) {
		this.olimpiadaService = olimpiadaService;
	}

	@GetMapping
	public ResponseEntity<List<OlimpiadaDtoResponse>> listAll() {
		return ResponseEntity.ok(olimpiadaService.listAll());
	}

	@PostMapping
	public ResponseEntity<OlimpiadaDtoResponse> create(@RequestBody OlimpiadaDtoRequest request) {
		return ResponseEntity.ok(olimpiadaService.create(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<OlimpiadaDtoResponse> update(@PathVariable Long id,
			@RequestBody OlimpiadaDtoRequest request) {
		return ResponseEntity.ok(olimpiadaService.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		olimpiadaService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
