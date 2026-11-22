package com.example.mobileapplication.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.data.repository.RepairRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The ViewModel for the Housing repair feature (the "VM" in MVVM).
 *
 * It holds the form state, validates it, asks the [RepairRepository] to save a
 * new request, exposes the saved requests for the "My repairs" list, and
 * remembers which request the confirmation and progress screens should show.
 * Screens only read its state and call its event functions.
 *
 * Saving and loading use the database, so they run in [viewModelScope] off the
 * main thread; [saveStatus] tells the confirmation screen while a save is running.
 *
 * Because a ViewModel survives screen rotation, the user does not lose what
 * they typed when the phone is turned.
 *
 * @param repository Where repair requests are stored. Tests can pass their own instance.
 */
class HousingViewModel(
    private val repository: RepairRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(RepairFormState())

    /** The current state of the repair form, observed by the form screen. */
    val formState: StateFlow<RepairFormState> = _formState.asStateFlow()

    private val _selectedRequest = MutableStateFlow<RepairRequest?>(null)

    /** The request shown on the confirmation and progress screens, or null if none is selected. */
    val selectedRequest: StateFlow<RepairRequest?> = _selectedRequest.asStateFlow()

    private val _saveStatus = MutableStateFlow(SaveStatus.IDLE)

    /** Whether a submitted request is still being saved, or failed to save. */
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    /**
     * Every saved repair request, newest first, for the "My repairs" list.
     *
     * It is null until the database has loaded, so the screen can tell
     * "still loading" apart from "no repairs yet" (an empty list).
     */
    val requests: StateFlow<List<RepairRequest>?> = repository.requests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    /** Called when the user picks a room. Also clears the room error. */
    fun onRoomSelected(room: Room) {
        _formState.update { it.copy(room = room, roomError = null) }
    }

    /** Called when the user picks a problem type. Also clears the problem error. */
    fun onIssueTypeSelected(issueType: IssueType) {
        _formState.update { it.copy(issueType = issueType, issueTypeError = null) }
    }

    /** Called every time the description text changes. Also clears the description error. */
    fun onDescriptionChanged(description: String) {
        _formState.update { it.copy(description = description, descriptionError = null) }
    }

    /** Called when the user picks an appointment preference. Also clears that error. */
    fun onAppointmentSelected(preference: AppointmentPreference) {
        _formState.update { it.copy(appointmentPreference = preference, appointmentError = null) }
    }

    /**
     * Called when the user taps "Next".
     *
     * On every step except the last, it checks that step's fields and moves to
     * the next step only if they are valid. On the last step it calls [submit].
     *
     * @return True only when the request was submitted, so the screen can move on.
     */
    fun onNext(): Boolean {
        val current = _formState.value
        if (current.isLastStep) return submit()

        val checked = RepairFormValidator.validateStep(current)
        _formState.value = if (checked.hasErrorOn(current.step)) {
            checked
        } else {
            checked.copy(step = RepairStep.entries[current.step.ordinal + 1])
        }
        return false
    }

    /**
     * Called when the user goes back inside the wizard.
     *
     * @return False when already on the first step, so the caller can leave the form instead.
     */
    fun onPreviousStep(): Boolean {
        val step = _formState.value.step
        if (step.ordinal == 0) return false
        _formState.update { it.copy(step = RepairStep.entries[step.ordinal - 1]) }
        return true
    }

    /**
     * Validates the form and, if everything is filled in correctly, starts saving
     * a new repair request.
     *
     * If the form is valid the function returns true straight away so the screen
     * can move on, and [saveStatus] is [SaveStatus.SAVING] until the database
     * write finishes. Then the new request becomes the [selectedRequest] and the
     * form is cleared. If the write fails, [saveStatus] becomes [SaveStatus.FAILED]
     * and the form keeps what the user typed so they can try again.
     *
     * If the form is invalid the error messages are shown, the wizard jumps back
     * to the first step with an error, and the function returns false.
     */
    fun submit(): Boolean {
        // Ignore a second tap while the first request is still being saved.
        if (_saveStatus.value == SaveStatus.SAVING) return false
        val checked = RepairFormValidator.validate(_formState.value)
        if (!checked.isValid) {
            val firstStepWithError = RepairStep.entries.first { checked.hasErrorOn(it) }
            _formState.value = checked.copy(step = firstStepWithError)
            return false
        }
        _formState.value = checked
        _selectedRequest.value = null
        _saveStatus.value = SaveStatus.SAVING
        viewModelScope.launch {
            try {
                // The validator guarantees these values are not null, so !! is safe here.
                val request = repository.addRequest(
                    room = checked.room!!,
                    issueType = checked.issueType!!,
                    description = checked.description.trim(),
                    appointmentPreference = checked.appointmentPreference!!
                )
                _selectedRequest.value = request
                _formState.value = RepairFormState()
                _saveStatus.value = SaveStatus.IDLE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.FAILED
            }
        }
        return true
    }

    /**
     * Chooses which saved request the progress screen should show, using its [reference].
     * The request is looked up in the database.
     */
    fun selectRequest(reference: String) {
        viewModelScope.launch {
            _selectedRequest.value = repository.getRequest(reference)
        }
    }

    /**
     * Chooses a request the screen already has (for example a row tapped in
     * "My repairs"), so the progress screen can show it without waiting for the database.
     */
    fun selectRequest(request: RepairRequest) {
        _selectedRequest.value = request
    }

    /** Creates the ViewModel with the app's shared, database-backed [RepairRepository]. */
    companion object {

        /** How long [requests] keeps listening after the screen stops, so rotation doesn't reload it. */
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        /** Factory used by `viewModel(factory = HousingViewModel.Factory)`. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HousingViewModel(RepairRepository.getInstance(this[APPLICATION_KEY]!!))
            }
        }
    }
}

/** The progress of saving a submitted repair request. */
enum class SaveStatus {
    /** Nothing is being saved (either nothing was submitted, or the last save succeeded). */
    IDLE,

    /** The request is being written to the database. */
    SAVING,

    /** The database write failed; the form still holds the user's answers. */
    FAILED
}
