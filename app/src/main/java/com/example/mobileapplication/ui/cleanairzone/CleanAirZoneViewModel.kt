package com.example.mobileapplication.ui.cleanairzone

import androidx.lifecycle.ViewModel
import com.example.mobileapplication.data.model.CazCheckResult
import com.example.mobileapplication.data.repository.SampleVehicleRepository
import com.example.mobileapplication.domain.cleanairzone.CazRulesEngine
import com.example.mobileapplication.domain.cleanairzone.RegistrationError
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidation
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Everything the Clean Air Zone checker screen shows.
 *
 * @property registration The text in the number-plate field, in capital letters.
 * @property error Why the last check was rejected, or null if there is no error.
 * @property result The prototype result of the last valid check, or null if
 * nothing has been checked since the text last changed.
 * @property officialCheckerFallbackUrl The official checker's web address, shown
 * as text when no app could open it; null when there is nothing to show.
 */
data class CleanAirZoneState(
    val registration: String = "",
    val error: RegistrationError? = null,
    val result: CazCheckResult? = null,
    val officialCheckerFallbackUrl: String? = null
)

/**
 * The ViewModel for the Clean Air Zone checker (the "VM" in MVVM).
 *
 * It holds what the user types, checks the format with [RegistrationValidator],
 * looks the plate up in the [SampleVehicleRepository] and asks [CazRulesEngine]
 * for a prototype result. The screen only reads [state] and calls the event functions.
 *
 * @param repository Where the sample vehicles come from. Tests can pass their own instance.
 */
class CleanAirZoneViewModel(
    private val repository: SampleVehicleRepository = SampleVehicleRepository.shared
) : ViewModel() {

    private val _state = MutableStateFlow(CleanAirZoneState())

    /** The current state of the checker, observed by the screen. */
    val state: StateFlow<CleanAirZoneState> = _state.asStateFlow()

    /** The demo registrations users can try, shown as a hint on the screen. */
    val demoRegistrations: List<String> get() = repository.registrations

    /**
     * Called every time the plate text changes. The text is shown in capitals,
     * and the old error and result are cleared because they no longer match it.
     */
    fun onRegistrationChanged(text: String) {
        _state.update { CleanAirZoneState(registration = text.uppercase()) }
    }

    /**
     * Called when the user taps "Check vehicle".
     *
     * An invalid entry sets [CleanAirZoneState.error]. A valid one sets
     * [CleanAirZoneState.result]; a plate that is not in the sample data gives an
     * unknown outcome rather than an error.
     */
    fun onCheck() {
        val entered = _state.value.registration
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

    /** Holds the official checker's address. */
    companion object {

        /** Bristol City Council's official Clean Air Zone charges and vehicle checker. */
        const val OFFICIAL_CHECKER_URL =
            "https://www.bristol.gov.uk/residents/streets-travel/bristols-caz/charges-and-vehicle-checker"
    }
}
