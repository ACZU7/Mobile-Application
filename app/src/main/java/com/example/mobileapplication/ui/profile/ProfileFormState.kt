package com.example.mobileapplication.ui.profile

import androidx.annotation.StringRes
import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidator

/**
 * What the user has typed on the Edit profile screen, and any error next to each field.
 *
 * Errors are string resource IDs, so the screen can show them from `strings.xml`.
 * A null error means the field is fine.
 */
data class ProfileFormState(
    val name: String = "",
    val email: String = "",
    val postcode: String = "",
    val vehicleRegistration: String = "",
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val postcodeError: Int? = null,
    @param:StringRes val vehicleError: Int? = null
) {
    /** True when no field has an error. */
    val isValid: Boolean
        get() = nameError == null && emailError == null && postcodeError == null && vehicleError == null

    companion object {
        /** Fills the form from a saved profile, so the user edits what is already there. */
        fun from(profile: UserProfile) = ProfileFormState(
            name = profile.name.orEmpty(),
            email = profile.email.orEmpty(),
            postcode = profile.postcode.orEmpty(),
            vehicleRegistration = profile.vehicleRegistration?.let(RegistrationValidator::format).orEmpty()
        )
    }
}
