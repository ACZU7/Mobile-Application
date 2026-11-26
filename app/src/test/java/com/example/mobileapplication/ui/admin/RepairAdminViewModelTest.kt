package com.example.mobileapplication.ui.admin

import com.example.mobileapplication.data.local.RepairRequestDao
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.data.repository.RepairRepository
import com.example.mobileapplication.testutil.FakeRepairRequestDao
import com.example.mobileapplication.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [RepairAdminViewModel]: loading, filters, status changes and
 * their failure, using the real [RepairRepository] on an in-memory DAO.
 * The repository seeds three samples: HR-2026-0001 (bathroom, completed),
 * HR-2026-0002 (bedroom, appointment booked) and HR-2026-0003 (kitchen, in progress).
 */
class RepairAdminViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dao = FakeRepairRequestDao()
    private val repository = RepairRepository(dao, today = { LocalDate.of(2026, 11, 26) })
    private val viewModel by lazy { RepairAdminViewModel(repository) }

    /** Keeps [RepairAdminViewModel.uiState] subscribed, as the screen would. */
    private fun TestScope.observe() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    @Test
    fun beforeSubscribing_itIsLoading() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun listsEveryRequest_newestFirst() = runTest {
        observe()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("HR-2026-0003", "HR-2026-0002", "HR-2026-0001"), state.requests.map { it.reference })
    }

    @Test
    fun filters_byStatusAndByRoom_andCombine() = runTest {
        observe()

        viewModel.onStatusFilterChanged(RepairStatus.COMPLETED)
        assertEquals(listOf("HR-2026-0001"), viewModel.uiState.value.requests.map { it.reference })

        viewModel.onStatusFilterChanged(null)
        viewModel.onRoomFilterChanged(Room.KITCHEN)
        assertEquals(listOf("HR-2026-0003"), viewModel.uiState.value.requests.map { it.reference })

        viewModel.onStatusFilterChanged(RepairStatus.COMPLETED)
        assertTrue(viewModel.uiState.value.requests.isEmpty())

        viewModel.onRoomFilterChanged(null)
        viewModel.onStatusFilterChanged(null)
        assertEquals(3, viewModel.uiState.value.requests.size)
    }

    @Test
    fun updateStatus_savesIt_showsSaved_andTheTenantSeesIt() = runTest {
        observe()

        viewModel.updateStatus("HR-2026-0003", RepairStatus.COMPLETED)

        val state = viewModel.uiState.value
        assertTrue(state.saved)
        assertFalse(state.saveFailed)
        assertNull(state.savingReference)
        assertEquals(RepairStatus.COMPLETED, state.requests.first { it.reference == "HR-2026-0003" }.status)
        // The tenant's screens read the same repository.
        assertEquals(RepairStatus.COMPLETED, repository.requests.first().first { it.reference == "HR-2026-0003" }.status)
    }

    @Test
    fun updatedRequest_movesBetweenStatusFilters() = runTest {
        observe()
        viewModel.onStatusFilterChanged(RepairStatus.IN_PROGRESS)
        assertEquals(listOf("HR-2026-0003"), viewModel.uiState.value.requests.map { it.reference })

        viewModel.updateStatus("HR-2026-0003", RepairStatus.COMPLETED)

        assertTrue(viewModel.uiState.value.requests.isEmpty())
    }

    @Test
    fun failedUpdate_showsAnError_andKeepsTheOldStatus() = runTest {
        observe()
        dao.failUpdates = true

        viewModel.updateStatus("HR-2026-0003", RepairStatus.COMPLETED)

        val state = viewModel.uiState.value
        assertTrue(state.saveFailed)
        assertFalse(state.saved)
        assertEquals(RepairStatus.IN_PROGRESS, state.requests.first { it.reference == "HR-2026-0003" }.status)
    }

    @Test
    fun unknownReference_isReportedAsAFailure() = runTest {
        observe()

        viewModel.updateStatus("HR-2026-9999", RepairStatus.COMPLETED)

        assertTrue(viewModel.uiState.value.saveFailed)
    }

    @Test
    fun counts_coverEveryRequest_byStatusAndRoom_evenWhenFiltered() = runTest {
        observe()
        viewModel.onRoomFilterChanged(Room.KITCHEN)

        val counts = viewModel.uiState.value.counts
        assertEquals(1, counts.byStatus[RepairStatus.COMPLETED])
        assertEquals(1, counts.byStatus[RepairStatus.APPOINTMENT_BOOKED])
        assertEquals(1, counts.byStatus[RepairStatus.IN_PROGRESS])
        assertEquals(0, counts.byStatus[RepairStatus.SUBMITTED])
        assertEquals(1, counts.byRoom[Room.BATHROOM])
        assertEquals(0, counts.byRoom[Room.HALLWAY])
        assertEquals(Room.entries.size, counts.byRoom.size)
    }

    @Test
    fun loadFailure_showsTheErrorState_andRetryLoadsAgain() = runTest {
        val failing = object : RepairRequestDao by dao {
            var fail = true
            override fun observeAll() =
                if (fail) flow { throw IllegalStateException("Simulated database failure") }
                else dao.observeAll()
        }
        val vm = RepairAdminViewModel(RepairRepository(failing))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }

        assertTrue(vm.uiState.value.loadFailed)
        assertFalse(vm.uiState.value.isLoading)

        failing.fail = false
        vm.onRetry()

        assertFalse(vm.uiState.value.loadFailed)
        assertEquals(3, vm.uiState.value.requests.size)
    }

    @Test
    fun noRequests_isAnEmptyList_notLoading() = runTest {
        observe()
        viewModel.onStatusFilterChanged(RepairStatus.SUBMITTED)

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.requests.isEmpty())
    }
}
