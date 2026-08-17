package com.amorif.exceptions;

public class ClosedSchoolYearException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClosedSchoolYearException(String message) {
		super(message);
	}
}
