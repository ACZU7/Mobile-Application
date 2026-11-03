package com.example.mobileapplication.domain.cleanairzone

/**
 * Checks that what the user typed looks like a UK registration.
 *
 * It only checks the format. It never tries to identify a real vehicle; the
 * Clean Air Zone checker looks the plate up in sample data only.
 *
 * It is plain Kotlin with no string resources, so it can be unit tested without
 * Android. The screen turns each [RegistrationError] into a message from `strings.xml`.
 */
object RegistrationValidator {

    /** Shortest UK registration, for example "A1". */
    const val MIN_LENGTH = 2

    /** Longest UK registration, for example "AB12CDE". */
    const val MAX_LENGTH = 7

    /**
     * Normalises [input] and checks its format.
     *
     * @return [RegistrationValidation.Valid] with the normalised plate, or
     * [RegistrationValidation.Invalid] with the first problem found.
     */
    fun validate(input: String): RegistrationValidation {
        val plate = normalise(input)
        val error = when {
            plate.isEmpty() -> RegistrationError.EMPTY
            plate.any { it !in 'A'..'Z' && it !in '0'..'9' } -> RegistrationError.INVALID_CHARACTERS
            plate.length < MIN_LENGTH -> RegistrationError.TOO_SHORT
            plate.length > MAX_LENGTH -> RegistrationError.TOO_LONG
            plate.none { it in '0'..'9' } -> RegistrationError.NO_DIGITS
            else -> return RegistrationValidation.Valid(plate)
        }
        return RegistrationValidation.Invalid(error)
    }

    /** Trims, removes all spaces and makes the letters upper case, so "ab12 cde" becomes "AB12CDE". */
    fun normalise(input: String): String = input.filterNot { it.isWhitespace() }.uppercase()
}

/** The result of [RegistrationValidator.validate]. */
sealed interface RegistrationValidation {

    /** The input is a valid UK registration format. [plate] is the normalised plate, for example "AB12CDE". */
    data class Valid(val plate: String) : RegistrationValidation

    /** The input is not a valid UK registration format, because of [error]. */
    data class Invalid(val error: RegistrationError) : RegistrationValidation
}

/** Why a registration was rejected. */
enum class RegistrationError {
    /** Nothing was entered. */
    EMPTY,

    /** Fewer than [RegistrationValidator.MIN_LENGTH] characters. */
    TOO_SHORT,

    /** More than [RegistrationValidator.MAX_LENGTH] characters. */
    TOO_LONG,

    /** Contains something other than the letters A–Z and the digits 0–9. */
    INVALID_CHARACTERS,

    /** Every UK registration has at least one digit. */
    NO_DIGITS
}
