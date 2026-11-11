package com.example.mobileapplication.ui.housing

import androidx.lifecycle.ViewModel
import com.example.mobileapplication.data.model.RentAccount
import com.example.mobileapplication.data.repository.RentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Everything the rent account screen needs to draw itself.
 *
 * @property account The (sample) rent account to show.
 * @property showPaymentUnavailable True when the "payments aren't available in
 * this prototype" message should be shown, after the user taps "Pay rent".
 */
data class RentUiState(
    val account: RentAccount,
    val showPaymentUnavailable: Boolean = false
)

/**
 * The ViewModel for the rent account screen (the "VM" in MVVM).
 *
 * It loads the sample account from the [RentRepository] and handles the
 * "Pay rent" button. The prototype must never take a payment, so tapping it
 * only asks the screen to show an explanation.
 *
 * @param repository Where the rent account comes from. Tests can pass one with a fixed date.
 */
class RentViewModel(
    repository: RentRepository = RentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentUiState(account = repository.getAccount()))

    /** The current screen state, observed by the rent account screen. */
    val uiState: StateFlow<RentUiState> = _uiState.asStateFlow()

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
}
