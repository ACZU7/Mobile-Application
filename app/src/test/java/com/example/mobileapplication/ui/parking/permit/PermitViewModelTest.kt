package com.example.mobileapplication.ui.parking.permit

import com.example.mobileapplication.data.model.ResidentPermit
import com.example.mobileapplication.data.repository.PermitStore
import com.example.mobileapplication.data.repository.SamplePermit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class PermitViewModelTest {

    private class FakeStore(permit: ResidentPermit = SamplePermit.permit) : PermitStore {
        override val permit = MutableStateFlow(permit)
        override val reminderEnabled = MutableStateFlow(false)
        override suspend fun setReminderEnabled(enabled: Boolean) { reminderEnabled.value = enabled }
    }

    private class FakeScheduler(var canSchedule: Boolean = true) : PermitReminderScheduler {
        var scheduled: ResidentPermit? = null
        var cancelled = 0
        override fun schedule(permit: ResidentPermit): Boolean {
            if (canSchedule) scheduled = permit
            return canSchedule
        }
        override fun cancel() { cancelled++; scheduled = null }
    }

    private lateinit var store: FakeStore
    private lateinit var scheduler: FakeScheduler
    private lateinit var viewModel: PermitViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        store = FakeStore()
        scheduler = FakeScheduler()
        viewModel = PermitViewModel(store, scheduler)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsStoredPermitWithReminderOff() {
        val state = viewModel.uiState.value
        assertEquals(SamplePermit.permit, state.permit)
        assertFalse(state.reminderEnabled)
        assertFalse(state.isLoading)
        assertEquals("CM", state.zone.code)
    }

    @Test
    fun enablingReminder_schedulesAndSavesTheChoice() {
        viewModel.enableReminder()
        assertEquals(SamplePermit.permit, scheduler.scheduled)
        assertTrue(store.reminderEnabled.value)
        assertTrue(viewModel.uiState.value.reminderEnabled)
        assertNull(viewModel.uiState.value.dialog)
    }

    @Test
    fun disablingReminder_cancelsAndSavesTheChoice() {
        viewModel.enableReminder()
        viewModel.disableReminder()
        assertEquals(1, scheduler.cancelled)
        assertNull(scheduler.scheduled)
        assertFalse(store.reminderEnabled.value)
        assertFalse(viewModel.uiState.value.reminderEnabled)
    }

    @Test
    fun notificationsBlocked_leavesReminderOffAndExplains() {
        viewModel.onNotificationsBlocked()
        assertEquals(PermitDialog.NOTIFICATIONS_BLOCKED, viewModel.uiState.value.dialog)
        assertFalse(viewModel.uiState.value.reminderEnabled)
        assertNull(scheduler.scheduled)
        viewModel.dismissDialog()
        assertNull(viewModel.uiState.value.dialog)
    }

    @Test
    fun expiredPermit_cannotBeRemindedAndExplains() {
        scheduler.canSchedule = false
        viewModel.enableReminder()
        assertFalse(store.reminderEnabled.value)
        assertEquals(PermitDialog.PERMIT_EXPIRED, viewModel.uiState.value.dialog)
    }

    @Test
    fun savedReminderChoice_isRestoredOnNextLaunch() {
        store.reminderEnabled.value = true
        val restarted = PermitViewModel(store, scheduler)
        assertTrue(restarted.uiState.value.reminderEnabled)
    }

    @Test
    fun storedPermitChanges_reachTheScreen() {
        val renewed = SamplePermit.permit.copy(expiryDate = LocalDate.of(2028, 3, 31))
        store.permit.value = renewed
        assertEquals(renewed, viewModel.uiState.value.permit)
    }

    @Test
    fun zoneAndPermitDetails_openAsDialogs() {
        viewModel.showZoneInfo()
        assertEquals(PermitDialog.ZONE_INFO, viewModel.uiState.value.dialog)
        viewModel.showPermitDetails()
        assertEquals(PermitDialog.PERMIT_DETAILS, viewModel.uiState.value.dialog)
    }
}
