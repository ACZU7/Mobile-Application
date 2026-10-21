package com.example.mobileapplication.ui.housing

import com.example.mobileapplication.R

/**
 * Checks the repair form's required fields.
 *
 * It is kept separate from the ViewModel and the screen so the rules can be
 * unit tested on their own without any Android UI.
 */
object RepairFormValidator {

    /** The shortest description we accept, so the repair team gets useful detail. */
    const val MIN_DESCRIPTION_LENGTH = 10

    /**
     * Returns a copy of [state] with an error message set on every invalid field
     * and cleared on every valid one.
     *
     * Rules: a room, an issue type and an appointment preference must be chosen,
     * and the description must have at least [MIN_DESCRIPTION_LENGTH] characters
     * (spaces at the start and end are not counted).
     */
    fun validate(state: RepairFormState): RepairFormState {
        val trimmedDescription = state.description.trim()
        return state.copy(
            roomError = if (state.room == null) R.string.housing_error_room else null,
            issueTypeError = if (state.issueType == null) R.string.housing_error_issue else null,
            descriptionError = when {
                trimmedDescription.isEmpty() -> R.string.housing_error_description_empty
                trimmedDescription.length < MIN_DESCRIPTION_LENGTH -> R.string.housing_error_description_short
                else -> null
            },
            appointmentError = if (state.appointmentPreference == null) R.string.housing_error_appointment else null
        )
    }

    /**
     * Checks only the fields on the wizard step [state] is currently showing, so
     * the user is not told about fields they have not reached yet.
     *
     * Errors on other steps are left as they are.
     */
    fun validateStep(state: RepairFormState): RepairFormState {
        val all = validate(state)
        return when (state.step) {
            RepairStep.ROOM -> state.copy(roomError = all.roomError)
            RepairStep.PROBLEM -> state.copy(
                issueTypeError = all.issueTypeError,
                descriptionError = all.descriptionError
            )
            RepairStep.PHOTOS -> state
            RepairStep.APPOINTMENT -> state.copy(appointmentError = all.appointmentError)
        }
    }
}
