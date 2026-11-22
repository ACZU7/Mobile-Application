package com.example.mobileapplication.ui.housing

import com.example.mobileapplication.data.model.UpcomingRentPayment
import com.example.mobileapplication.data.repository.RentReminderSettings
import com.example.mobileapplication.data.repository.RentRepository
import com.example.mobileapplication.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** Unit tests for [RentViewModel], its reminder switch and the rent formatting helpers. */
class RentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /** Keeps the reminder choice in memory instead of DataStore. */
    private class FakeSettings(initial: Boolean = false) : RentReminderSettings {
        override val reminderEnabled = MutableStateFlow(initial)
        override suspend fun setReminderEnabled(enabled: Boolean) {
            reminderEnabled.value = enabled
        }
    }

    /** Records what the ViewModel asked WorkManager to do. */
    private class FakeScheduler(private val canSchedule: Boolean = true) : RentReminderScheduler {
        val scheduled = mutableListOf<UpcomingRentPayment>()
        var cancelCount = 0
        override fun schedule(payment: UpcomingRentPayment): Boolean {
            if (canSchedule) scheduled += payment
            return canSchedule
        }
        override fun cancel() {
            cancelCount++
        }
    }

    private val today = LocalDate.of(2026, 11, 11)
    private val settings = FakeSettings()
    private val scheduler = FakeScheduler()
    // Lazy, so it is created after MainDispatcherRule has set the test Main dispatcher.
    private val viewModel by lazy { RentViewModel(RentRepository(today = { today }), settings, scheduler) }

    @Test
    fun initialState_showsSampleAccountWithoutPaymentMessage() {
        val state = viewModel.uiState.value

        assertEquals(RentRepository.sampleAccount(today), state.account)
        assertTrue(state.account.isSample)
        assertFalse(state.showPaymentUnavailable)
    }

    @Test
    fun payRent_onlyShowsUnavailableMessageAndLeavesAccountUnchanged() {
        val before = viewModel.uiState.value.account

        viewModel.onPayRentClicked()

        val state = viewModel.uiState.value
        assertTrue(state.showPaymentUnavailable)
        assertEquals(before, state.account)
    }

    @Test
    fun messageShown_hidesTheMessage() {
        viewModel.onPayRentClicked()
        viewModel.onPaymentMessageShown()
        assertFalse(viewModel.uiState.value.showPaymentUnavailable)
    }

    @Test
    fun payRentTwice_stillShowsTheMessageAgain() {
        viewModel.onPayRentClicked()
        viewModel.onPaymentMessageShown()
        viewModel.onPayRentClicked()
        assertTrue(viewModel.uiState.value.showPaymentUnavailable)
    }

    @Test
    fun formatPence_showsPoundsAndPence() {
        assertEquals("£520.00", formatPence(52_000L))
        assertEquals("£0.00", formatPence(0L))
        assertEquals("£1,234.05", formatPence(123_405L))
    }

    @Test
    fun formatLongDate_writesMonthInFull() {
        assertEquals("1 December 2026", formatLongDate(LocalDate.of(2026, 12, 1)))
    }

    @Test
    fun initialState_reminderOffWhenNothingSaved() {
        val state = viewModel.uiState.value
        assertFalse(state.reminderEnabled)
        assertNull(state.reminderDialog)
    }

    @Test
    fun savedReminderChoice_isShownWhenScreenOpensAgain() {
        val reopened = RentViewModel(RentRepository(today = { today }), FakeSettings(initial = true), FakeScheduler())
        assertTrue(reopened.uiState.value.reminderEnabled)
    }

    @Test
    fun reminderDate_isThreeDaysBeforeNextPayment() {
        // The sample next payment is 1 December 2026.
        assertEquals(LocalDate.of(2026, 11, 28), viewModel.uiState.value.reminderDate)
    }

    @Test
    fun switchOn_showsOnButDoesNotScheduleOrSaveBeforePermission() {
        viewModel.onReminderSwitchedOn()

        assertTrue(viewModel.uiState.value.reminderEnabled)
        assertTrue(viewModel.uiState.value.awaitingPermission)
        assertTrue(scheduler.scheduled.isEmpty())
        assertFalse(settings.reminderEnabled.value)
    }

    @Test
    fun permissionAllowed_schedulesNextPaymentAndSavesChoice() {
        viewModel.onReminderSwitchedOn()
        viewModel.onNotificationsAllowed()

        assertEquals(listOf(viewModel.uiState.value.account.nextPayment), scheduler.scheduled)
        assertTrue(settings.reminderEnabled.value)
        assertTrue(viewModel.uiState.value.reminderEnabled)
        assertFalse(viewModel.uiState.value.awaitingPermission)
        assertNull(viewModel.uiState.value.reminderDialog)
    }

    @Test
    fun permissionDenied_turnsSwitchBackOffAndExplains() {
        viewModel.onReminderSwitchedOn()
        viewModel.onNotificationsBlocked()

        val state = viewModel.uiState.value
        assertFalse(state.reminderEnabled)
        assertFalse(state.awaitingPermission)
        assertEquals(RentReminderDialog.NOTIFICATIONS_BLOCKED, state.reminderDialog)
        assertTrue(scheduler.scheduled.isEmpty())
        assertFalse(settings.reminderEnabled.value)
    }

    @Test
    fun paymentAlreadyDue_keepsSwitchOffAndExplains() {
        val tooLate = FakeScheduler(canSchedule = false)
        val vm = RentViewModel(RentRepository(today = { today }), settings, tooLate)

        vm.onReminderSwitchedOn()
        vm.onNotificationsAllowed()

        assertFalse(vm.uiState.value.reminderEnabled)
        assertEquals(RentReminderDialog.PAYMENT_ALREADY_DUE, vm.uiState.value.reminderDialog)
        assertFalse(settings.reminderEnabled.value)
    }

    @Test
    fun switchOff_cancelsReminderAndSavesChoice() {
        viewModel.onReminderSwitchedOn()
        viewModel.onNotificationsAllowed()

        viewModel.onReminderSwitchedOff()

        assertEquals(1, scheduler.cancelCount)
        assertFalse(settings.reminderEnabled.value)
        assertFalse(viewModel.uiState.value.reminderEnabled)
    }

    @Test
    fun dismissDialog_clearsIt() {
        viewModel.onReminderSwitchedOn()
        viewModel.onNotificationsBlocked()
        viewModel.onReminderDialogDismissed()
        assertNull(viewModel.uiState.value.reminderDialog)
    }
}
