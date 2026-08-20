package com.amorif.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amorif.dto.request.PontuacaoDtoRequest;
import com.amorif.dto.request.TurmaDtoRequest;
import com.amorif.dto.response.PontuacaoDtoResponse;
import com.amorif.dto.response.TurmaDtoResponse;
import com.amorif.services.ManagerService;

/**
 * @author osailton
 */

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

	private final ManagerService managerService;

	public ManagerController(ManagerService managerService) {
		this.managerService = managerService;
	}

	@PostMapping("turma")
	public ResponseEntity<TurmaDtoResponse> register(@RequestBody TurmaDtoRequest request) {
		// Create new turma
		return ResponseEntity.ok().body(this.managerService.createTurma(request));
	}

	@PostMapping("aprovar")
	public ResponseEntity<PontuacaoDtoResponse> approvePoints(@RequestBody PontuacaoDtoRequest request) {
		return ResponseEntity.ok().body(this.managerService.approvePoints(request));
	}
	
	@PostMapping("aprovarTodas")
	public ResponseEntity<List<PontuacaoDtoResponse>> approveAllPoints(@RequestBody List<PontuacaoDtoRequest> requests) {
		return ResponseEntity.ok().body(this.managerService.approveAllPoints(requests));
	}
	
	@PostMapping("cancelar")
	public ResponseEntity<PontuacaoDtoResponse> cancelPoints(@RequestBody PontuacaoDtoRequest request) {
		return ResponseEntity.ok().body(this.managerService.cancelPoints(request));
	}

}
