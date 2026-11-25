package com.example.mobileapplication.ui.cleanairzone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.data.model.CazCheckResult
import com.example.mobileapplication.data.repository.DataStoreProfileRepository
import com.example.mobileapplication.data.repository.SampleVehicleRepository
import com.example.mobileapplication.domain.cleanairzone.CazRulesEngine
import com.example.mobileapplication.domain.cleanairzone.RegistrationError
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidation
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Everything the Clean Air Zone checker screen shows.
 *
 * @property registration The text in the number-plate field, in capital letters.
 * @property error Why the last check was rejected, or null if there is no error.
 * @property result The prototype result of the last valid check, or null if
 * nothing has been checked since the text last changed.
 * @property officialCheckerFallbackUrl The official checker's web address, shown
 * as text when no app could open it; null when there is nothing to show.
 * @property savedRegistration The vehicle saved in the demo profile, formatted
 * for display (for example "AB12 CDE"), or null if none is saved. When set, the
 * screen offers a "Check my saved vehicle" chip.
 */
data class CleanAirZoneState(
    val registration: String = "",
    val error: RegistrationError? = null,
    val result: CazCheckResult? = null,
    val officialCheckerFallbackUrl: String? = null,
    val savedRegistration: String? = null
)

/**
 * The ViewModel for the Clean Air Zone checker (the "VM" in MVVM).
 *
 * It holds what the user types, checks the format with [RegistrationValidator],
 * looks the plate up in the [SampleVehicleRepository] and asks [CazRulesEngine]
 * for a prototype result. The screen only reads [state] and calls the event functions.
 *
 * @param repository Where the sample vehicles come from. Tests can pass their own instance.
 * @param savedVehicle The registration saved in the demo profile (null when none),
 * so the user can fill it in with one tap.
 */
class CleanAirZoneViewModel(
    private val repository: SampleVehicleRepository = SampleVehicleRepository.shared,
    savedVehicle: Flow<String?> = flowOf(null)
) : ViewModel() {

    private val _state = MutableStateFlow(CleanAirZoneState())

    /** The current state of the checker, observed by the screen. */
    val state: StateFlow<CleanAirZoneState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            savedVehicle
                // A profile that can't be read just means no chip.
                .catch { error -> if (error is CancellationException) throw error else emit(null) }
                .collect { plate ->
                    _state.update { it.copy(savedRegistration = plate?.let(RegistrationValidator::format)) }
                }
        }
    }

    /** The demo registrations users can try, shown as a hint on the screen. */
    val demoRegistrations: List<String> get() = repository.registrations

    /**
     * Called every time the plate text changes. The old error and result are
     * cleared because they no longer match it.
     *
     * The text is kept exactly as typed: changing it here while the keyboard is
     * still composing can lose letters. The screen shows it in capitals, and
     * [onCheck] converts it.
     */
    fun onRegistrationChanged(text: String) {
        _state.update { CleanAirZoneState(registration = text, savedRegistration = it.savedRegistration) }
    }

    /**
     * Called when the user taps "Check my saved vehicle": fills the plate field
     * with the registration from the demo profile.
     */
    fun onUseSavedVehicle() {
        val saved = _state.value.savedRegistration ?: return
        onRegistrationChanged(saved)
    }

    /**
     * Called when the user taps "Check vehicle".
     *
     * An invalid entry sets [CleanAirZoneState.error]. A valid one sets
     * [CleanAirZoneState.result]; a plate that is not in the sample data gives an
     * unknown outcome rather than an error.
     */
    fun onCheck() {
        val entered = _state.value.registration.uppercase()
        when (val validation = RegistrationValidator.validate(entered)) {
            is RegistrationValidation.Invalid -> _state.update {
                it.copy(error = validation.error, result = null)
            }
            is RegistrationValidation.Valid -> {
                val vehicle = repository.findByRegistration(validation.plate)
                val result = CazRulesEngine.check(entered.trim(), vehicle)
                _state.update { it.copy(error = null, result = result) }
            }
        }
    }

    /**
     * Called when the user taps "Use the official checker". Opens
     * [OFFICIAL_CHECKER_URL] with [linkOpener]; if no app can open it, the
     * address is put in the state so the screen can show it instead.
     */
    fun onOpenOfficialChecker(linkOpener: LinkOpener) {
        val opened = linkOpener.open(OFFICIAL_CHECKER_URL)
        _state.update { it.copy(officialCheckerFallbackUrl = if (opened) null else OFFICIAL_CHECKER_URL) }
    }

    /** Holds the official checker's address and the factory. */
    companion object {

        /** Factory used by `viewModel(factory = CleanAirZoneViewModel.Factory)`: adds the saved profile vehicle. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CleanAirZoneViewModel(
                    savedVehicle = DataStoreProfileRepository(this[APPLICATION_KEY]!!).profile
                        .map { it.vehicleRegistration }
                        .distinctUntilChanged()
                )
            }
        }

        /** Bristol City Council's official Clean Air Zone charges and vehicle checker. */
        const val OFFICIAL_CHECKER_URL =
            "https://www.bristol.gov.uk/residents/streets-travel/bristols-caz/charges-and-vehicle-checker"
    }
}
