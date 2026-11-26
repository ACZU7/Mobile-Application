package com.example.mobileapplication.ui.auth

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.Role
import com.example.mobileapplication.data.model.Session
import com.example.mobileapplication.data.repository.AuthRepository
import com.example.mobileapplication.testutil.FakeAuthStore
import com.example.mobileapplication.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Unit tests for [LoginViewModel]: field validation, wrong passwords, the
 * lockout, and signing in as each role. The real [AuthRepository] runs on an
 * in-memory store, so the hashing and lockout logic are tested too.
 */
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private var nowMillis = 5_000_000L
    private val store = FakeAuthStore()
    private val repository = AuthRepository(store, now = { nowMillis }, hashContext = EmptyCoroutineContext)
    private val viewModel by lazy { LoginViewModel(repository) }

    private fun attempt(email: String, password: String) {
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
        viewModel.onSignIn()
    }

    @Test
    fun emptyFields_showAnErrorNextToEach_andDontTryToSignIn() {
        viewModel.onSignIn()

        val state = viewModel.state.value
        assertEquals(R.string.login_error_email_empty, state.emailError)
        assertEquals(R.string.login_error_password_empty, state.passwordError)
        assertNull(state.error)
        assertEquals(0, store.current.failedAttempts)
    }

    @Test
    fun badlyFormattedEmail_showsTheFormatError() {
        attempt("resident@", "Resident-Demo-2026")

        assertEquals(R.string.login_error_email_format, viewModel.state.value.emailError)
        assertNull(viewModel.state.value.passwordError)
        assertNull(store.current.session)
    }

    @Test
    fun typing_clearsThatFieldsError() {
        viewModel.onSignIn()
        viewModel.onEmailChanged("r")

        assertNull(viewModel.state.value.emailError)
        assertEquals(R.string.login_error_password_empty, viewModel.state.value.passwordError)
    }

    @Test
    fun wrongPassword_showsOneGeneralMessage_andClearsThePassword() {
        attempt("resident@example.com", "wrong-password")

        val state = viewModel.state.value
        assertEquals(LoginError.InvalidCredentials, state.error)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertEquals("", state.password)
        assertEquals("resident@example.com", state.email)
        assertFalse(state.isSigningIn)
        assertNull(store.current.session)
    }

    @Test
    fun unknownEmail_showsTheSameMessageAsAWrongPassword() {
        attempt("someone@example.com", "Resident-Demo-2026")

        assertEquals(LoginError.InvalidCredentials, viewModel.state.value.error)
    }

    @Test
    fun fiveWrongAttempts_blockSignInForThirtySeconds() {
        repeat(4) {
            attempt("admin@example.com", "wrong")
            assertEquals(LoginError.InvalidCredentials, viewModel.state.value.error)
        }

        attempt("admin@example.com", "wrong")
        assertEquals(LoginError.LockedOut(30), viewModel.state.value.error)

        // Even the right password is refused while blocked.
        nowMillis += 12_000
        attempt("admin@example.com", "Admin-Demo-2026")
        assertEquals(LoginError.LockedOut(18), viewModel.state.value.error)
        assertNull(store.current.session)

        nowMillis += 18_000
        attempt("admin@example.com", "Admin-Demo-2026")
        assertEquals(Session("admin@example.com", Role.ADMIN), store.current.session)
    }

    @Test
    fun resident_signsInAsResident_andTheFormResets() {
        attempt("resident@example.com", "Resident-Demo-2026")

        assertEquals(Session("resident@example.com", Role.RESIDENT), store.current.session)
        assertEquals(LoginUiState(), viewModel.state.value)
    }

    @Test
    fun admin_signsInAsAdmin_ignoringCaseAndSpacesInTheEmail() {
        attempt(" ADMIN@example.com ", "Admin-Demo-2026")

        assertEquals(Session("admin@example.com", Role.ADMIN), store.current.session)
    }

    @Test
    fun continueAsGuest_savesAGuestSession() {
        viewModel.onContinueAsGuest()

        assertEquals(Session.Guest, store.current.session)
        assertEquals(Role.GUEST, store.current.session?.role)
    }

    @Test
    fun eyeButton_togglesPasswordVisibility() {
        assertFalse(viewModel.state.value.passwordVisible)
        viewModel.onTogglePasswordVisibility()
        assertTrue(viewModel.state.value.passwordVisible)
        viewModel.onTogglePasswordVisibility()
        assertFalse(viewModel.state.value.passwordVisible)
    }
}
