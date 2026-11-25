package com.example.mobileapplication.ui.profile

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.domain.cleanairzone.RegistrationError
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidation
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidator

/**
 * Checks the Edit profile form. Every field is optional: an empty field is
 * valid and is saved as "not set".
 *
 * - Email must look like `name@example.com`.
 * - Postcode must be a UK postcode, for example "BS1 5TR".
 * - Vehicle registration uses the Clean Air Zone [RegistrationValidator].
 *
 * Plain Kotlin with no Android calls, so it can be unit tested on the JVM.
 */
object ProfileFormValidator {

    /** Longest name accepted, so the greeting and avatar stay tidy. */
    const val NAME_MAX_LENGTH = 50

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /**
     * UK postcode in its standard form ("outward inward", one space), for example
     * "BS1 5TR", "SW1A 1AA" or "M1 1AE". Based on the format published by the
     * Office for National Statistics; it checks the shape, not that the postcode exists.
     */
    private val POSTCODE = Regex(
        "^(GIR 0AA|[A-PR-UWYZ]([0-9][0-9A-HJKPSTUW]?|[A-HK-Y][0-9][0-9ABEHMNPRV-Y]?) [0-9][ABD-HJLNP-UW-Z]{2})$"
    )

    /** Returns [form] with an error set on every field that is wrong, and the others cleared. */
    fun validate(form: ProfileFormState): ProfileFormState = form.copy(
        nameError = when {
            form.name.trim().length > NAME_MAX_LENGTH -> R.string.profile_error_name_too_long
            else -> null
        },
        emailError = when {
            form.email.isBlank() -> null
            !isValidEmail(form.email) -> R.string.profile_error_email
            else -> null
        },
        postcodeError = when {
            form.postcode.isBlank() -> null
            normalisePostcode(form.postcode) == null -> R.string.profile_error_postcode
            else -> null
        },
        vehicleError = when {
            form.vehicleRegistration.isBlank() -> null
            else -> (RegistrationValidator.validate(form.vehicleRegistration) as? RegistrationValidation.Invalid)
                ?.error?.messageRes
        }
    )

    /**
     * Turns a valid form into the profile to save: text trimmed, blank fields
     * removed, postcode and registration in their standard form.
     * Call only after [validate] found no errors.
     */
    fun toProfile(form: ProfileFormState): UserProfile = UserProfile(
        name = form.name.trim().replace(Regex("\\s+"), " ").ifEmpty { null },
        email = form.email.trim().ifEmpty { null },
        postcode = form.postcode.takeIf { it.isNotBlank() }?.let(::normalisePostcode),
        vehicleRegistration = form.vehicleRegistration.takeIf { it.isNotBlank() }
            ?.let(RegistrationValidator::normalise)
    )

    /** True when [input] (after trimming) looks like an email address. */
    fun isValidEmail(input: String): Boolean = EMAIL.matches(input.trim())

    /**
     * Returns [input] as a standard UK postcode, for example "bs15tr" becomes
     * "BS1 5TR", or null if it is not a valid postcode.
     */
    fun normalisePostcode(input: String): String? {
        val compact = input.filterNot { it.isWhitespace() }.uppercase()
        if (compact.length !in 5..7) return null
        val formatted = compact.dropLast(3) + " " + compact.takeLast(3)
        return formatted.takeIf { POSTCODE.matches(it) }
    }

    /** The message for each registration problem. The CAZ checker's wording is reused. */
    private val RegistrationError.messageRes: Int
        get() = when (this) {
            RegistrationError.EMPTY -> R.string.caz_error_empty
            RegistrationError.TOO_SHORT -> R.string.caz_error_too_short
            RegistrationError.TOO_LONG -> R.string.caz_error_too_long
            RegistrationError.INVALID_CHARACTERS -> R.string.caz_error_invalid_characters
            RegistrationError.NO_DIGITS -> R.string.caz_error_no_digits
        }
}
