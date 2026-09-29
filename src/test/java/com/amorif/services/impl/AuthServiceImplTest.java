package com.amorif.services.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthServiceImplTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"FG2 - COTIC/LAJ",
			"CD2 - DG/LAJ",
			"SUB-CHEFIA - DG/LAJ",
			"CD4 - DIAC/LAJ"
	})
	void shouldRecognizeCurrentSuapFunctionsAsAdministrator(String function) {
		assertTrue(AuthServiceImpl.hasAdminFunction(new String[] { function }));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"FG0002 - COTIC/LAJ",
			"CD0002 - DG/LAJ",
			"CD0004 - DIAC/LAJ"
	})
	void shouldNotRecognizeObsoleteSuapFunctionsAsAdministrator(String function) {
		assertFalse(AuthServiceImpl.hasAdminFunction(new String[] { function }));
	}
}
