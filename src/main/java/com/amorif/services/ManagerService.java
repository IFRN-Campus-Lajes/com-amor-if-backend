package com.amorif.services;

import java.util.List;

import com.amorif.dto.request.PontuacaoDtoRequest;
import com.amorif.dto.request.TurmaDtoRequest;
import com.amorif.dto.response.PontuacaoDtoResponse;
import com.amorif.dto.response.TurmaDtoResponse;

/**
 * @author osailton
 */

public interface ManagerService {
	
	TurmaDtoResponse createTurma(TurmaDtoRequest request);
	PontuacaoDtoResponse approvePoints(PontuacaoDtoRequest request);
	List<PontuacaoDtoResponse> approveAllPoints(List<PontuacaoDtoRequest> requests);
	PontuacaoDtoResponse cancelPoints(PontuacaoDtoRequest request);

}
