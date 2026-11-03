package com.example.mobileapplication.domain.cleanairzone

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for the registration format checks in [RegistrationValidator]. */
class RegistrationValidatorTest {

    private fun errorOf(input: String): RegistrationError? =
        (RegistrationValidator.validate(input) as? RegistrationValidation.Invalid)?.error

    // Normalising

    @Test
    fun lowerCaseWithSpace_isNormalisedToUpperCaseWithoutSpaces() {
        assertEquals(RegistrationValidation.Valid("AB12CDE"), RegistrationValidator.validate("ab12 cde"))
    }

    @Test
    fun leadingTrailingAndInnerSpaces_areRemoved() {
        assertEquals(RegistrationValidation.Valid("AB12CDE"), RegistrationValidator.validate("  AB 12  CDE \n"))
    }

    // Valid lengths

    @Test
    fun twoCharacters_isValid() {
        assertEquals(RegistrationValidation.Valid("A1"), RegistrationValidator.validate("a1"))
    }

    @Test
    fun sevenCharacters_isValid() {
        assertEquals(RegistrationValidation.Valid("IB15PTL"), RegistrationValidator.validate("IB15 PTL"))
    }

    // Errors

    @Test
    fun emptyInput_isEmptyError() {
        assertEquals(RegistrationError.EMPTY, errorOf(""))
    }

    @Test
    fun onlySpaces_isEmptyError() {
        assertEquals(RegistrationError.EMPTY, errorOf("   "))
    }

    @Test
    fun oneCharacter_isTooShort() {
        assertEquals(RegistrationError.TOO_SHORT, errorOf("1"))
    }

    @Test
    fun eightCharacters_isTooLong() {
        assertEquals(RegistrationError.TOO_LONG, errorOf("AB12 CDEF"))
    }

    @Test
    fun punctuation_isInvalidCharacters() {
        assertEquals(RegistrationError.INVALID_CHARACTERS, errorOf("AB12-CDE"))
    }

    @Test
    fun nonEnglishLetters_areInvalidCharacters() {
        assertEquals(RegistrationError.INVALID_CHARACTERS, errorOf("ÅB12CDE"))
    }

    @Test
    fun lettersOnly_isNoDigitsError() {
        assertEquals(RegistrationError.NO_DIGITS, errorOf("abcdef"))
    }
}
