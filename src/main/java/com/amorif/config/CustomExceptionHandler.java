package com.amorif.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.amorif.dto.response.ErrorMessageDtoResponse;
import com.amorif.exceptions.AnnualRuleException;
import com.amorif.exceptions.AnnualRulePerStudentException;
import com.amorif.exceptions.BimonthlyRuleException;
import com.amorif.exceptions.BimonthlyRulePerStudentException;
import com.amorif.exceptions.ClosedSchoolYearException;
import com.amorif.exceptions.InvalidArgumentException;
import com.amorif.exceptions.InvalidBimesterException;
import com.amorif.exceptions.InvalidExtraBimesterException;
import com.amorif.exceptions.InvalidFixedValueException;
import com.amorif.exceptions.InvalidJWTAuthenticationException;
import com.amorif.exceptions.InvalidSchoolRegistrationException;
import com.amorif.exceptions.InvalidTurnException;
import com.amorif.exceptions.InvalidVariableValueException;
import com.amorif.exceptions.PointsAlreadyCancelledOrAppliedException;
import com.amorif.exceptions.PointsNotFoundException;
import com.amorif.exceptions.RuleNotFoundException;
import com.amorif.exceptions.TurmaNotFoundException;
import com.amorif.exceptions.UserAlreadyExistsException;
import com.amorif.exceptions.UserHasNoPermitedRoleException;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(ClosedSchoolYearException.class)
	public ResponseEntity<Object> handleClosedSchoolYearException(ClosedSchoolYearException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.CONFLICT, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<Object> handleUsernameAvailableException(UserAlreadyExistsException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(InvalidJWTAuthenticationException.class)
	public ResponseEntity<Object> handleUsernameAvailableException(InvalidJWTAuthenticationException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(InvalidArgumentException.class)
	public ResponseEntity<Object> handleInvalidArgumentException(InvalidArgumentException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(UserHasNoPermitedRoleException.class)
	public ResponseEntity<Object> handleUserHasNoPermitedRoleException(UserHasNoPermitedRoleException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.FORBIDDEN, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(InvalidBimesterException.class)
	public ResponseEntity<Object> handleInvalidBimesterException(InvalidBimesterException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}	
	
	@ExceptionHandler(InvalidExtraBimesterException.class)
	public ResponseEntity<Object> handleInvalidExtraBimesterException(InvalidExtraBimesterException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}	
	
	@ExceptionHandler(InvalidFixedValueException.class)
	public ResponseEntity<Object> handleInvalidFixedValueException(InvalidFixedValueException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}	
	
	@ExceptionHandler(InvalidVariableValueException.class)
	public ResponseEntity<Object> handleInvalidVariableValueException(InvalidVariableValueException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(AnnualRuleException.class)
	public ResponseEntity<Object> handleAnnualRuleException(AnnualRuleException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(BimonthlyRuleException.class)
	public ResponseEntity<Object> handleBimonthlyRuleException(BimonthlyRuleException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(InvalidTurnException.class)
	public ResponseEntity<Object> handleInvalidTurnException(InvalidTurnException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(BimonthlyRulePerStudentException.class)
	public ResponseEntity<Object> handleBimonthlyRulePerStudentException(BimonthlyRulePerStudentException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(AnnualRulePerStudentException.class)
	public ResponseEntity<Object> handleAnnualRulePerStudentException(AnnualRulePerStudentException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(InvalidSchoolRegistrationException.class)
	public ResponseEntity<Object> handleInvalidSchoolRegistrationException(InvalidSchoolRegistrationException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}

	@ExceptionHandler(RuleNotFoundException.class)
	public ResponseEntity<Object> handleRuleNotFoundException(RuleNotFoundException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(PointsAlreadyCancelledOrAppliedException.class)
	public ResponseEntity<Object> handleRuleAlreadyCancelledOrAppliedException(PointsAlreadyCancelledOrAppliedException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(PointsNotFoundException.class)
	public ResponseEntity<Object> handlePointsNotFoundException(PointsNotFoundException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
	@ExceptionHandler(TurmaNotFoundException.class)
	public ResponseEntity<Object> handleTurmaNotFoundException(TurmaNotFoundException e, WebRequest request) {
		ErrorMessageDtoResponse eMessage = new ErrorMessageDtoResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		return new ResponseEntity<Object>(eMessage, new HttpHeaders(), eMessage.getStatus());
	}
	
}
