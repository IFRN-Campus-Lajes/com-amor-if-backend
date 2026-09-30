package com.amorif.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthServiceImplTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"FG2 - COTIC/LAJ",
			"FG0002 - COTIC/LAJ",
			"CD2 - DG/LAJ",
			"CD0002 - DG/LAJ",
			"SUB-CHEFIA - DG/LAJ",
			"CD4 - DIAC/LAJ",
			"CD0004 - DIAC/LAJ"
	})
	void shouldRecognizeSuapFunctionsWithOrWithoutLeadingZerosAsAdministrator(String function) {
		assertTrue(AuthServiceImpl.hasAdminFunction(new String[] { function }));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"FG3 - COTIC/LAJ",
			"CD2 - OUTRO/LAJ",
			"CD4 - DG/LAJ"
	})
	void shouldNotRecognizeDifferentSuapFunctionsAsAdministrator(String function) {
		assertFalse(AuthServiceImpl.hasAdminFunction(new String[] { function }));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"FG0007 - SETOR/LAJ",
			"CD0007 - SETOR/LAJ"
	})
	void shouldApplyTheSameNormalizationToOtherFunctionBasedRoles(String function) {
		String expectedFunction = function.substring(0, 2) + "7 - SETOR/LAJ";

		assertTrue(AuthServiceImpl.hasSuapFunction(
				new String[] { function }, List.of(expectedFunction)));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"FG0002 - COTIC/LAJ=FG2 - COTIC/LAJ",
			"CD0002 - DG/LAJ=CD2 - DG/LAJ",
			"CD0004 - DIAC/LAJ=CD4 - DIAC/LAJ",
			"SUB-CHEFIA - DG/LAJ=SUB-CHEFIA - DG/LAJ"
	})
	void shouldNormalizeOnlyCdAndFgNumericCodes(String scenario) {
		String[] values = scenario.split("=", 2);

		assertEquals(values[1], AuthServiceImpl.normalizeSuapFunctionCode(values[0]));
	}
}
