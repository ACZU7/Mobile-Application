package com.example.mobileapplication.ui.housing

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.data.repository.RepairRepository
import com.example.mobileapplication.testutil.FakeRepairRequestDao
import com.example.mobileapplication.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for the events and submit flow in [HousingViewModel].
 *
 * The repository runs on an in-memory [FakeRepairRequestDao], so no device is needed.
 */
class HousingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeRepairRequestDao()
    private val repository = RepairRepository(dao, today = { LocalDate.of(2026, 10, 21) })

    // Created lazily so it is built after MainDispatcherRule has installed the test dispatcher.
    private val viewModel by lazy { HousingViewModel(repository) }

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
        assertTrue(dao.stored.none { !it.isSample })
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
        assertEquals(4, dao.stored.size)
        assertEquals(SaveStatus.IDLE, viewModel.saveStatus.value)
    }

    @Test
    fun validSubmit_resetsTheForm() {
        fillValidForm()
        viewModel.submit()
        assertEquals(RepairFormState(), viewModel.formState.value)
    }

    @Test
    fun failedSave_reportsFailureAndKeepsTheUsersAnswers() {
        dao.failInserts = true
        fillValidForm()

        assertTrue(viewModel.submit())

        assertEquals(SaveStatus.FAILED, viewModel.saveStatus.value)
        assertNull(viewModel.selectedRequest.value)
        assertEquals(Room.BATHROOM, viewModel.formState.value.room)
    }

    @Test
    fun changingAField_clearsItsError() {
        viewModel.submit()
        viewModel.onDescriptionChanged("x")
        assertNull(viewModel.formState.value.descriptionError)
    }

    @Test
    fun next_withoutRoom_staysOnRoomStepAndShowsOnlyRoomError() {
        assertFalse(viewModel.onNext())

        val state = viewModel.formState.value
        assertEquals(RepairStep.ROOM, state.step)
        assertEquals(R.string.housing_error_room, state.roomError)
        assertNull(state.issueTypeError)
        assertNull(state.appointmentError)
    }

    @Test
    fun next_withValidStep_movesToNextStep() {
        viewModel.onRoomSelected(Room.KITCHEN)
        viewModel.onNext()
        assertEquals(RepairStep.PROBLEM, viewModel.formState.value.step)
    }

    @Test
    fun next_onProblemStep_checksDescription() {
        viewModel.onRoomSelected(Room.KITCHEN)
        viewModel.onNext()
        viewModel.onIssueTypeSelected(IssueType.LEAK)
        viewModel.onDescriptionChanged("Leak")
        viewModel.onNext()

        val state = viewModel.formState.value
        assertEquals(RepairStep.PROBLEM, state.step)
        assertEquals(R.string.housing_error_description_short, state.descriptionError)
    }

    @Test
    fun next_throughEveryStep_submitsOnLastStep() {
        fillValidForm()
        repeat(RepairStep.entries.size - 1) { assertFalse(viewModel.onNext()) }
        assertEquals(RepairStep.APPOINTMENT, viewModel.formState.value.step)

        assertTrue(viewModel.onNext())
        assertEquals(4, dao.stored.size)
    }

    @Test
    fun previousStep_goesBackAndReturnsFalseOnFirstStep() {
        viewModel.onRoomSelected(Room.KITCHEN)
        viewModel.onNext()

        assertTrue(viewModel.onPreviousStep())
        assertEquals(RepairStep.ROOM, viewModel.formState.value.step)
        assertFalse(viewModel.onPreviousStep())
    }

    @Test
    fun invalidSubmit_jumpsToFirstStepWithAnError() {
        viewModel.onRoomSelected(Room.KITCHEN)
        viewModel.onAppointmentSelected(AppointmentPreference.MORNING)

        assertFalse(viewModel.submit())
        assertEquals(RepairStep.PROBLEM, viewModel.formState.value.step)
    }

    @Test
    fun selectRequest_selectsSampleByReference() {
        viewModel.selectRequest("HR-2026-0002")
        assertEquals(Room.BEDROOM, viewModel.selectedRequest.value?.room)
    }

    @Test
    fun selectRequest_withRequestObject_selectsItImmediately() {
        val sample = RepairRepository.sampleRequests().first()
        viewModel.selectRequest(sample)
        assertEquals(sample, viewModel.selectedRequest.value)
    }

    @Test
    fun requests_listsSamplesThenIncludesNewSubmission() = runTest {
        // stateIn(WhileSubscribed) only loads while something is collecting, like a screen would.
        backgroundScope.launch(mainDispatcherRule.dispatcher) { viewModel.requests.collect() }

        assertEquals(3, viewModel.requests.value?.size)

        fillValidForm()
        viewModel.submit()

        val requests = viewModel.requests.value
        assertNotNull(requests)
        assertEquals(4, requests!!.size)
        assertEquals("HR-2026-0004", requests.first().reference)
    }
}
