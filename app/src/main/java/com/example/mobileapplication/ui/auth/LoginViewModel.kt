package com.example.mobileapplication.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobileapplication.R
import com.example.mobileapplication.data.repository.AuthRepository
import com.example.mobileapplication.data.repository.SignInResult
import com.example.mobileapplication.ui.profile.ProfileFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A message about the whole sign-in attempt, rather than one field. */
sealed interface LoginError {
    /** Wrong email or password. Deliberately doesn't say which. */
    data object InvalidCredentials : LoginError

    /** Too many wrong attempts; sign-in is blocked for [secondsLeft] seconds. */
    data class LockedOut(val secondsLeft: Int) : LoginError
}

/**
 * Everything the Login screen shows.
 *
 * Field errors are string resource IDs shown under their field, as on the other forms.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    val error: LoginError? = null,
    val isSigningIn: Boolean = false
)

/**
 * The ViewModel for the Login screen (the "VM" in MVVM).
 *
 * It checks the fields, asks the [AuthRepository] to sign in, and shows the
 * result. It does not navigate: the app shell watches the saved session and
 * moves to Home once someone is signed in.
 */
class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())

    /** The state of the Login screen. */
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Called when the email text changes. Clears its error and the general message. */
    fun onEmailChanged(text: String) = _state.update { it.copy(email = text, emailError = null, error = null) }

    /** Called when the password text changes. Clears its error and the general message. */
    fun onPasswordChanged(text: String) = _state.update { it.copy(password = text, passwordError = null, error = null) }

    /** Called when the eye button is tapped: shows or hides the password. */
    fun onTogglePasswordVisibility() = _state.update { it.copy(passwordVisible = !it.passwordVisible) }

    /** Called when the user taps "Sign in" or the keyboard's Done key. */
    fun onSignIn() {
        val current = _state.value
        if (current.isSigningIn) return
        val emailError = when {
            current.email.isBlank() -> R.string.login_error_email_empty
            !ProfileFormValidator.isValidEmail(current.email) -> R.string.login_error_email_format
            else -> null
        }
        val passwordError = if (current.password.isEmpty()) R.string.login_error_password_empty else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError, error = null) }
            return
        }

        _state.update { it.copy(isSigningIn = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.signIn(current.email, current.password)) {
                // The shell navigates away once the session is saved.
                is SignInResult.Success -> _state.update { LoginUiState() }
                SignInResult.InvalidCredentials -> _state.update {
                    it.copy(isSigningIn = false, password = "", error = LoginError.InvalidCredentials)
                }
                is SignInResult.LockedOut -> _state.update {
                    it.copy(isSigningIn = false, password = "", error = LoginError.LockedOut(result.secondsLeft))
                }
            }
        }
    }

    /** Called when the user taps "Continue as guest". */
    fun onContinueAsGuest() {
        viewModelScope.launch { repository.continueAsGuest() }
    }

    /** Creates the ViewModel with the app's shared [AuthRepository]. */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(AuthRepository.getInstance(this[APPLICATION_KEY]!!)) }
        }
    }
}
