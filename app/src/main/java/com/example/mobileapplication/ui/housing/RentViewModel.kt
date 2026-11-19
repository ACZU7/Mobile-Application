package com.example.mobileapplication.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.data.model.RentAccount
import com.example.mobileapplication.data.repository.DataStoreRentReminderSettings
import com.example.mobileapplication.data.repository.RentReminderSettings
import com.example.mobileapplication.data.repository.RentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** A message the rent screen shows in a dialog about the reminder. */
enum class RentReminderDialog {
    /** Notification permission was refused (or notifications are off), so the reminder was turned off. */
    NOTIFICATIONS_BLOCKED,

    /** The next payment is already due, so there is nothing left to remind about. */
    PAYMENT_ALREADY_DUE
}

/**
 * Everything the rent account screen needs to draw itself.
 *
 * @property account The (sample) rent account to show.
 * @property showPaymentUnavailable True when the "payments aren't available in
 * this prototype" message should be shown, after the user taps "Pay rent".
 * @property reminderEnabled True when the "Remind me before rent is due" switch is on.
 * It is also true while the notification permission prompt is open.
 * @property awaitingPermission True while the notification permission prompt is open,
 * so the screen doesn't say the reminder is set before it really is.
 * @property reminderDialog The reminder message to show, or null for none.
 */
data class RentUiState(
    val account: RentAccount,
    val showPaymentUnavailable: Boolean = false,
    val reminderEnabled: Boolean = false,
    val awaitingPermission: Boolean = false,
    val reminderDialog: RentReminderDialog? = null
) {
    /** The day the reminder will be shown for the next payment. */
    val reminderDate: LocalDate
        get() = RentReminderTiming.reminderTime(account.nextPayment.dueDate).toLocalDate()
}

/**
 * The ViewModel for the rent account screen (the "VM" in MVVM).
 *
 * It loads the sample account from the [RentRepository], handles the "Pay rent"
 * button (the prototype never takes a payment) and the rent reminder switch:
 * the choice is saved in [settings] and the reminder is set or cancelled with [scheduler].
 *
 * Notification permission is handled by the screen. The order is:
 * [onReminderSwitchedOn] → the screen asks for permission → [onNotificationsAllowed]
 * or [onNotificationsBlocked].
 *
 * @param repository Where the rent account comes from. Tests can pass one with a fixed date.
 * @param settings Where the reminder choice is saved (DataStore in the app, a fake in tests).
 * @param scheduler Sets and cancels the reminder (WorkManager in the app, a fake in tests).
 */
class RentViewModel(
    repository: RentRepository,
    private val settings: RentReminderSettings,
    private val scheduler: RentReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentUiState(account = repository.getAccount()))

    /** The current screen state, observed by the rent account screen. */
    val uiState: StateFlow<RentUiState> = _uiState.asStateFlow()

    init {
        // Show the saved switch position, so it is still on after the app is reopened.
        viewModelScope.launch {
            settings.reminderEnabled.collect { enabled ->
                _uiState.update { it.copy(reminderEnabled = enabled) }
            }
        }
    }

    /**
     * Called when the user taps "Pay rent". No payment is taken; the screen is
     * asked to explain that payments aren't available in this prototype.
     */
    fun onPayRentClicked() {
        _uiState.update { it.copy(showPaymentUnavailable = true) }
    }

    /** Called by the screen once it has shown the message, so it isn't shown again (for example after rotation). */
    fun onPaymentMessageShown() {
        _uiState.update { it.copy(showPaymentUnavailable = false) }
    }

    /**
     * The user turned the switch on. It shows as on straight away; the screen then
     * asks for notification permission. Nothing is saved until permission is given.
     */
    fun onReminderSwitchedOn() {
        _uiState.update { it.copy(reminderEnabled = true, awaitingPermission = true) }
    }

    /** Notifications are allowed: set the reminder and save the choice. */
    fun onNotificationsAllowed() {
        if (scheduler.schedule(_uiState.value.account.nextPayment)) {
            _uiState.update { it.copy(reminderEnabled = true, awaitingPermission = false) }
            viewModelScope.launch { settings.setReminderEnabled(true) }
        } else {
            _uiState.update {
                it.copy(
                    reminderEnabled = false,
                    awaitingPermission = false,
                    reminderDialog = RentReminderDialog.PAYMENT_ALREADY_DUE
                )
            }
        }
    }

    /** Permission was refused or notifications are off: turn the switch back off and explain. */
    fun onNotificationsBlocked() {
        scheduler.cancel()
        _uiState.update {
            it.copy(
                reminderEnabled = false,
                awaitingPermission = false,
                reminderDialog = RentReminderDialog.NOTIFICATIONS_BLOCKED
            )
        }
        viewModelScope.launch { settings.setReminderEnabled(false) }
    }

    /** The user turned the switch off: cancel the reminder and save the choice. */
    fun onReminderSwitchedOff() {
        scheduler.cancel()
        _uiState.update { it.copy(reminderEnabled = false) }
        viewModelScope.launch { settings.setReminderEnabled(false) }
    }

    /** Called when the reminder dialog is closed. */
    fun onReminderDialogDismissed() {
        _uiState.update { it.copy(reminderDialog = null) }
    }

    companion object {
        /** Factory used by `viewModel(factory = RentViewModel.Factory)`. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY]!!
                RentViewModel(
                    repository = RentRepository(),
                    settings = DataStoreRentReminderSettings(app),
                    scheduler = WorkManagerRentReminderScheduler(app)
                )
            }
        }
    }
}
