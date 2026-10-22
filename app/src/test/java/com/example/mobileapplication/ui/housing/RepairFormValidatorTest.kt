package com.example.mobileapplication.ui.housing

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the repair form rules in [RepairFormValidator]. */
class RepairFormValidatorTest {

    private val validState = RepairFormState(
        room = Room.KITCHEN,
        issueType = IssueType.LEAK,
        description = "Tap is leaking constantly",
        appointmentPreference = AppointmentPreference.MORNING
    )

    @Test
    fun validForm_hasNoErrors() {
        assertTrue(RepairFormValidator.validate(validState).isValid)
    }

    @Test
    fun emptyForm_showsAnErrorOnEveryField() {
        val result = RepairFormValidator.validate(RepairFormState())

        assertFalse(result.isValid)
        assertEquals(R.string.housing_error_room, result.roomError)
        assertEquals(R.string.housing_error_issue, result.issueTypeError)
        assertEquals(R.string.housing_error_description_empty, result.descriptionError)
        assertEquals(R.string.housing_error_appointment, result.appointmentError)
    }

    @Test
    fun missingRoom_showsOnlyRoomError() {
        val result = RepairFormValidator.validate(validState.copy(room = null))

        assertEquals(R.string.housing_error_room, result.roomError)
        assertNull(result.issueTypeError)
        assertNull(result.descriptionError)
        assertNull(result.appointmentError)
    }

    @Test
    fun missingIssueType_showsIssueError() {
        val result = RepairFormValidator.validate(validState.copy(issueType = null))
        assertEquals(R.string.housing_error_issue, result.issueTypeError)
    }

    @Test
    fun missingAppointment_showsAppointmentError() {
        val result = RepairFormValidator.validate(validState.copy(appointmentPreference = null))
        assertEquals(R.string.housing_error_appointment, result.appointmentError)
    }

    @Test
    fun descriptionOfNineCharacters_isTooShort() {
        val result = RepairFormValidator.validate(validState.copy(description = "123456789"))
        assertEquals(R.string.housing_error_description_short, result.descriptionError)
    }

    @Test
    fun descriptionOfTenCharacters_isAccepted() {
        val result = RepairFormValidator.validate(validState.copy(description = "1234567890"))
        assertNull(result.descriptionError)
    }

    @Test
    fun whitespaceOnlyDescription_countsAsEmpty() {
        val result = RepairFormValidator.validate(validState.copy(description = "           "))
        assertEquals(R.string.housing_error_description_empty, result.descriptionError)
    }

    @Test
    fun surroundingSpaces_areNotCounted() {
        val result = RepairFormValidator.validate(validState.copy(description = "   short   "))
        assertEquals(R.string.housing_error_description_short, result.descriptionError)
    }

    @Test
    fun fixingAField_clearsItsPreviousError() {
        val withErrors = RepairFormValidator.validate(RepairFormState())
        val fixed = RepairFormValidator.validate(withErrors.copy(room = Room.BEDROOM))
        assertNull(fixed.roomError)
    }
}
