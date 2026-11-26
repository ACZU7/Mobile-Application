package com.example.mobileapplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.data.repository.RepairRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * How many repair requests there are with each status and in each room, for Analytics.
 * Every status and room is listed, with 0 where there are none.
 */
data class RepairCounts(
    val byStatus: Map<RepairStatus, Int>,
    val byRoom: Map<Room, Int>
) {
    companion object {
        /** Counts [requests] by status and by room. */
        fun of(requests: List<RepairRequest>) = RepairCounts(
            byStatus = RepairStatus.entries.associateWith { status -> requests.count { it.status == status } },
            byRoom = Room.entries.associateWith { room -> requests.count { it.room == room } }
        )

        val Empty = of(emptyList())
    }
}

/**
 * Everything the admin Repairs tab shows.
 *
 * @property isLoading True until the requests have been read from the database.
 * @property loadFailed True if they could not be read; the screen offers Retry.
 * @property requests The requests that match the filters, newest first.
 * @property statusFilter Only show requests with this status, or null for all.
 * @property roomFilter Only show requests in this room, or null for all.
 * @property savingReference The request whose new status is being saved, or null.
 * @property saveFailed True if the last status change could not be saved.
 * @property saved True after a status change was saved ("Saved on this device").
 * @property counts Counts over every request (not only the filtered ones), for Analytics.
 */
data class RepairAdminUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val requests: List<RepairRequest> = emptyList(),
    val statusFilter: RepairStatus? = null,
    val roomFilter: Room? = null,
    val savingReference: String? = null,
    val saveFailed: Boolean = false,
    val saved: Boolean = false,
    val counts: RepairCounts = RepairCounts.Empty
)

/**
 * The ViewModel for the admin Repairs tab (the "VM" in MVVM).
 *
 * It lists every housing repair request from the Room database, filters them by
 * status and room, and changes a request's status. The tenant's "My repairs" and
 * progress screens read the same database, so they show the new status too.
 *
 * Everything is local demonstration data: nothing is sent to Bristol City Council.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RepairAdminViewModel(private val repository: RepairRepository) : ViewModel() {

    /** The result of reading the requests from the database. */
    private sealed interface Load {
        data object Loading : Load
        data object Failed : Load
        data class Loaded(val requests: List<RepairRequest>) : Load
    }

    /** Bumped by [onRetry] to read the database again. */
    private val attempt = MutableStateFlow(0)

    private val load = attempt.flatMapLatest {
        repository.requests
            .map<List<RepairRequest>, Load> { Load.Loaded(it) }
            .onStart { emit(Load.Loading) }
            .catch { error -> if (error is CancellationException) throw error else emit(Load.Failed) }
    }

    /** Filters and save progress: everything that isn't the list itself. */
    private val controls = MutableStateFlow(RepairAdminUiState(isLoading = false))

    /** The state of the Repairs tab. */
    val uiState: StateFlow<RepairAdminUiState> = combine(load, controls) { result, extra ->
        when (result) {
            Load.Loading -> extra.copy(isLoading = true, loadFailed = false, requests = emptyList())
            Load.Failed -> extra.copy(isLoading = false, loadFailed = true, requests = emptyList())
            is Load.Loaded -> extra.copy(
                isLoading = false,
                loadFailed = false,
                requests = result.requests.filter { request ->
                    (extra.statusFilter == null || request.status == extra.statusFilter) &&
                        (extra.roomFilter == null || request.room == extra.roomFilter)
                },
                counts = RepairCounts.of(result.requests)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RepairAdminUiState())

    /** Called when an admin picks a status filter (null shows every status). */
    fun onStatusFilterChanged(status: RepairStatus?) = controls.update { it.copy(statusFilter = status) }

    /** Called when an admin picks a room filter (null shows every room). */
    fun onRoomFilterChanged(room: Room?) = controls.update { it.copy(roomFilter = room) }

    /** Called when an admin taps Retry after the list failed to load. */
    fun onRetry() = attempt.update { it + 1 }

    /**
     * Called when an admin taps Update: saves [status] for the request with
     * [reference]. Ignored while another change is still being saved.
     */
    fun updateStatus(reference: String, status: RepairStatus) {
        if (controls.value.savingReference != null) return
        controls.update { it.copy(savingReference = reference, saveFailed = false, saved = false) }
        viewModelScope.launch {
            val ok = try {
                repository.updateStatus(reference, status)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
            controls.update { it.copy(savingReference = null, saveFailed = !ok, saved = ok) }
        }
    }

    /** Creates the ViewModel with the app's shared, database-backed [RepairRepository]. */
    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { RepairAdminViewModel(RepairRepository.getInstance(this[APPLICATION_KEY]!!)) }
        }
    }
}
