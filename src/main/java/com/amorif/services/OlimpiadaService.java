package com.amorif.services;

import java.util.List;

import com.amorif.dto.request.OlimpiadaDtoRequest;
import com.amorif.dto.response.OlimpiadaDtoResponse;

public interface OlimpiadaService {
	List<OlimpiadaDtoResponse> listAll();
	OlimpiadaDtoResponse create(OlimpiadaDtoRequest request);
	OlimpiadaDtoResponse update(Long id, OlimpiadaDtoRequest request);
	void delete(Long id);
}
