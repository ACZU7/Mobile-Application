package com.example.mobileapplication.ui.parking.permit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapplication.data.model.ParkingZone
import com.example.mobileapplication.data.model.ResidentPermit
import com.example.mobileapplication.data.repository.PermitStore
import com.example.mobileapplication.data.repository.SamplePermit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Dialogs the permit screen can show. */
enum class PermitDialog { ZONE_INFO, PERMIT_DETAILS, NOTIFICATIONS_BLOCKED, PERMIT_EXPIRED }

data class PermitUiState(
    val zone: ParkingZone = SamplePermit.zone,
    val permit: ResidentPermit? = null,
    val reminderEnabled: Boolean = false,
    val dialog: PermitDialog? = null
) {
    val isLoading: Boolean get() = permit == null
}

/**
 * Screen 4: sample zone result, the stored resident's permit and the expiry-reminder switch.
 * Notification permission is handled by the screen; this only acts on the outcome.
 */
class PermitViewModel(
    private val store: PermitStore,
    private val scheduler: PermitReminderScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(PermitUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(store.permit, store.reminderEnabled, ::Pair).collect { (permit, enabled) ->
                _uiState.update { it.copy(permit = permit, reminderEnabled = enabled) }
            }
        }
    }

    /** Called only once notifications are allowed. */
    fun enableReminder() {
        val permit = _uiState.value.permit ?: return
        viewModelScope.launch {
            if (scheduler.schedule(permit)) {
                store.setReminderEnabled(true)
            } else {
                store.setReminderEnabled(false)
                _uiState.update { it.copy(dialog = PermitDialog.PERMIT_EXPIRED) }
            }
        }
    }

    fun disableReminder() {
        scheduler.cancel()
        viewModelScope.launch { store.setReminderEnabled(false) }
    }

    /** Permission refused or notifications blocked: leave the switch off and explain. */
    fun onNotificationsBlocked() {
        _uiState.update { it.copy(dialog = PermitDialog.NOTIFICATIONS_BLOCKED) }
    }

    fun showZoneInfo() = _uiState.update { it.copy(dialog = PermitDialog.ZONE_INFO) }

    fun showPermitDetails() = _uiState.update { it.copy(dialog = PermitDialog.PERMIT_DETAILS) }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }
}
