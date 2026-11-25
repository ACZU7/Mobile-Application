package com.example.mobileapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.data.repository.DataStoreProfileRepository
import com.example.mobileapplication.data.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Everything the Profile screen shows.
 *
 * @property profile The saved profile, or null while it is still loading.
 * @property showClearDialog True while the "Clear my data" confirmation is open.
 * @property message A one-off message to show in a snackbar, or null.
 */
data class ProfileUiState(
    val profile: UserProfile? = null,
    val showClearDialog: Boolean = false,
    val message: ProfileMessage? = null
)

/** One-off messages the Profile screen shows after an action. */
enum class ProfileMessage {
    SAVED,
    CLEARED,
    SAVE_FAILED,
    CLEAR_FAILED
}

/**
 * The ViewModel for the Profile and Edit profile screens (the "VM" in MVVM).
 *
 * It reads the saved demo profile from the [ProfileRepository], holds what the
 * user types on the edit form, validates it with [ProfileFormValidator], saves
 * it, and clears it after the user confirms.
 *
 * Both screens share one instance (scoped to the profile navigation graph).
 */
class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val dialogAndMessage = MutableStateFlow(ProfileUiState())

    /** The state of the Profile screen. */
    val uiState: StateFlow<ProfileUiState> =
        combine(repository.profile, dialogAndMessage) { profile, extra -> extra.copy(profile = profile) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ProfileUiState())

    private val _form = MutableStateFlow(ProfileFormState())

    /** The state of the Edit profile form. */
    val form: StateFlow<ProfileFormState> = _form.asStateFlow()

    /** Called when the user opens the Edit screen: fills the form with the saved values. */
    fun startEditing() {
        _form.value = ProfileFormState.from(uiState.value.profile ?: UserProfile())
    }

    /** Called when the name text changes. Also clears its error. */
    fun onNameChanged(text: String) = _form.update { it.copy(name = text, nameError = null) }

    /** Called when the email text changes. Also clears its error. */
    fun onEmailChanged(text: String) = _form.update { it.copy(email = text, emailError = null) }

    /**
     * Called when the postcode text changes. Also clears its error.
     *
     * The text is kept exactly as typed (changing it here while the keyboard is
     * still composing can lose letters); the screen shows it in capitals, and
     * [save] stores the standard form, for example "BS1 5TR".
     */
    fun onPostcodeChanged(text: String) = _form.update { it.copy(postcode = text, postcodeError = null) }

    /** Called when the registration text changes. Also clears its error. Kept as typed, like the postcode. */
    fun onVehicleChanged(text: String) = _form.update { it.copy(vehicleRegistration = text, vehicleError = null) }

    /**
     * Called when the user taps Save. Shows an error next to every wrong field;
     * if there are none, saves the profile in the background.
     *
     * @return True when the form was valid, so the screen can close.
     */
    fun save(): Boolean {
        val checked = ProfileFormValidator.validate(_form.value)
        _form.value = checked
        if (!checked.isValid) return false
        val profile = ProfileFormValidator.toProfile(checked)
        viewModelScope.launch {
            val message = try {
                repository.save(profile)
                ProfileMessage.SAVED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ProfileMessage.SAVE_FAILED
            }
            dialogAndMessage.update { it.copy(message = message) }
        }
        return true
    }

    /** Called when the user taps "Clear my data": asks for confirmation first. */
    fun onClearRequested() = dialogAndMessage.update { it.copy(showClearDialog = true) }

    /** Called when the user cancels the confirmation. Nothing is deleted. */
    fun onClearDismissed() = dialogAndMessage.update { it.copy(showClearDialog = false) }

    /** Called when the user confirms: deletes the profile from the phone. */
    fun onClearConfirmed() {
        dialogAndMessage.update { it.copy(showClearDialog = false) }
        viewModelScope.launch {
            val message = try {
                repository.clear()
                _form.value = ProfileFormState()
                ProfileMessage.CLEARED
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ProfileMessage.CLEAR_FAILED
            }
            dialogAndMessage.update { it.copy(message = message) }
        }
    }

    /** Called once the screen has shown [ProfileUiState.message]. */
    fun onMessageShown() = dialogAndMessage.update { it.copy(message = null) }

    /** Creates the ViewModel with the DataStore-backed repository. */
    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        /** Factory used by `viewModel(factory = ProfileViewModel.Factory)`. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ProfileViewModel(DataStoreProfileRepository(this[APPLICATION_KEY]!!)) }
        }
    }
}
