package com.example.mobileapplication.ui.housing

import androidx.lifecycle.ViewModel
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.data.repository.RepairRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The ViewModel for the Housing repair feature (the "VM" in MVVM).
 *
 * It holds the form state, validates it, asks the [RepairRepository] to save a
 * new request, and remembers which request the confirmation and progress
 * screens should show. Screens only read its state and call its event functions.
 *
 * Because a ViewModel survives screen rotation, the user does not lose what
 * they typed when the phone is turned.
 *
 * @param repository Where repair requests are stored. Tests can pass their own instance.
 */
class HousingViewModel(
    private val repository: RepairRepository = RepairRepository.shared
) : ViewModel() {

    private val _formState = MutableStateFlow(RepairFormState())

    /** The current state of the repair form, observed by the form screen. */
    val formState: StateFlow<RepairFormState> = _formState.asStateFlow()

    private val _selectedRequest = MutableStateFlow<RepairRequest?>(null)

    /** The request shown on the confirmation and progress screens, or null if none is selected. */
    val selectedRequest: StateFlow<RepairRequest?> = _selectedRequest.asStateFlow()

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
     * Validates the form and, if everything is filled in correctly, saves a new
     * repair request.
     *
     * On success the new request becomes the [selectedRequest], the form is
     * cleared and the function returns true so the screen can move on.
     * On failure the error messages are shown, the wizard jumps back to the first
     * step with an error, and the function returns false.
     */
    fun submit(): Boolean {
        val checked = RepairFormValidator.validate(_formState.value)
        if (!checked.isValid) {
            val firstStepWithError = RepairStep.entries.first { checked.hasErrorOn(it) }
            _formState.value = checked.copy(step = firstStepWithError)
            return false
        }
        // The validator guarantees these values are not null, so !! is safe here.
        val request = repository.addRequest(
            room = checked.room!!,
            issueType = checked.issueType!!,
            description = checked.description.trim(),
            appointmentPreference = checked.appointmentPreference!!
        )
        _selectedRequest.value = request
        _formState.value = RepairFormState()
        return true
    }

    /** Chooses which saved request the progress screen should show, using its [reference]. */
    fun selectRequest(reference: String) {
        _selectedRequest.value = repository.getRequest(reference)
    }
}
