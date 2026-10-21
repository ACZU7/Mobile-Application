package com.example.mobileapplication.ui.housing

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.data.repository.RepairRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Unit tests for the events and submit flow in [HousingViewModel]. */
class HousingViewModelTest {

    private val repository = RepairRepository(today = { LocalDate.of(2026, 10, 21) })
    private val viewModel = HousingViewModel(repository)

    private fun fillValidForm() {
        viewModel.onRoomSelected(Room.BATHROOM)
        viewModel.onIssueTypeSelected(IssueType.LEAK)
        viewModel.onDescriptionChanged("  Shower is leaking through the ceiling  ")
        viewModel.onAppointmentSelected(AppointmentPreference.AFTERNOON)
    }

    @Test
    fun invalidSubmit_returnsFalseShowsErrorsAndSavesNothing() {
        assertFalse(viewModel.submit())

        assertEquals(R.string.housing_error_room, viewModel.formState.value.roomError)
        assertEquals(3, repository.requests.value.size)
        assertNull(viewModel.selectedRequest.value)
    }

    @Test
    fun validSubmit_savesRequestAndSelectsIt() {
        fillValidForm()

        assertTrue(viewModel.submit())

        val selected = viewModel.selectedRequest.value!!
        assertEquals("HR-2026-0004", selected.reference)
        assertEquals(Room.BATHROOM, selected.room)
        assertEquals("Shower is leaking through the ceiling", selected.description)
        assertEquals(4, repository.requests.value.size)
    }

    @Test
    fun validSubmit_resetsTheForm() {
        fillValidForm()
        viewModel.submit()
        assertEquals(RepairFormState(), viewModel.formState.value)
    }

    @Test
    fun changingAField_clearsItsError() {
        viewModel.submit()
        viewModel.onDescriptionChanged("x")
        assertNull(viewModel.formState.value.descriptionError)
    }

    @Test
    fun selectRequest_selectsSampleByReference() {
        viewModel.selectRequest("HR-2026-0002")
        assertEquals(Room.BEDROOM, viewModel.selectedRequest.value?.room)
    }
}
