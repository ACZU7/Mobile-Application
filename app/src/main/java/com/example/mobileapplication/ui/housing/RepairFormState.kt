package com.example.mobileapplication.ui.housing

import androidx.annotation.StringRes
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room

/**
 * Everything the repair form screen needs to draw itself.
 *
 * The screen only reads this state; it never changes it directly. Instead it
 * sends events to [HousingViewModel], which creates a new copy of this state.
 *
 * Each `...Error` field holds a string resource ID for the error message to show
 * under that field, or null when the field has no error.
 */
data class RepairFormState(
    val room: Room? = null,
    val issueType: IssueType? = null,
    val description: String = "",
    val appointmentPreference: AppointmentPreference? = null,
    @StringRes val roomError: Int? = null,
    @StringRes val issueTypeError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val appointmentError: Int? = null
) {
    /** True when no field currently shows an error. */
    val isValid: Boolean
        get() = roomError == null && issueTypeError == null &&
            descriptionError == null && appointmentError == null
}
