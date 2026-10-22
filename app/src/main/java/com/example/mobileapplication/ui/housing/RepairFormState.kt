package com.example.mobileapplication.ui.housing

import androidx.annotation.StringRes
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room

/**
 * The steps of the repair wizard, in the order the user sees them.
 *
 * [labelRes] is the short label shown under the stepper circle.
 */
enum class RepairStep(@StringRes val labelRes: Int) {
    ROOM(R.string.housing_step_room),
    PROBLEM(R.string.housing_step_problem),
    PHOTOS(R.string.housing_step_photos),
    APPOINTMENT(R.string.housing_step_appointment)
}

/**
 * Everything the repair form screen needs to draw itself.
 *
 * The screen only reads this state; it never changes it directly. Instead it
 * sends events to [HousingViewModel], which creates a new copy of this state.
 *
 * Each `...Error` field holds a string resource ID for the error message to show
 * under that field, or null when the field has no error.
 *
 * @property step The wizard step currently on screen.
 */
data class RepairFormState(
    val room: Room? = null,
    val issueType: IssueType? = null,
    val description: String = "",
    val appointmentPreference: AppointmentPreference? = null,
    @StringRes val roomError: Int? = null,
    @StringRes val issueTypeError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val appointmentError: Int? = null,
    val step: RepairStep = RepairStep.ROOM
) {
    /** True when no field currently shows an error. */
    val isValid: Boolean
        get() = roomError == null && issueTypeError == null &&
            descriptionError == null && appointmentError == null

    /** True when the current step is the last one, so "Next" should submit the request. */
    val isLastStep: Boolean
        get() = step == RepairStep.entries.last()

    /** True when a field that belongs to [step] shows an error. */
    fun hasErrorOn(step: RepairStep): Boolean = when (step) {
        RepairStep.ROOM -> roomError != null
        RepairStep.PROBLEM -> issueTypeError != null || descriptionError != null
        RepairStep.PHOTOS -> false
        RepairStep.APPOINTMENT -> appointmentError != null
    }
}
