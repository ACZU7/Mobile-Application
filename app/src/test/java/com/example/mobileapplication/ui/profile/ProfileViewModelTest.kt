package com.example.mobileapplication.ui.profile

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.testutil.FakeProfileRepository
import com.example.mobileapplication.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Unit tests for [ProfileViewModel], using an in-memory [FakeProfileRepository]. */
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val saved = UserProfile(
        name = "Sam Wilson",
        email = "sam@example.com",
        postcode = "BS1 5TR",
        vehicleRegistration = "IB15PTL"
    )

    /** Keeps [ProfileViewModel.uiState] subscribed, as the screen would. */
    private fun TestScope.observe(viewModel: ProfileViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    @Test
    fun uiState_showsTheSavedProfile() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository(saved))
        observe(viewModel)

        assertEquals(saved, viewModel.uiState.value.profile)
    }

    @Test
    fun startEditing_fillsTheFormWithTheSavedValues() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository(saved))
        observe(viewModel)

        viewModel.startEditing()

        val form = viewModel.form.value
        assertEquals("Sam Wilson", form.name)
        assertEquals("sam@example.com", form.email)
        assertEquals("BS1 5TR", form.postcode)
        assertEquals("IB15 PTL", form.vehicleRegistration)
    }

    @Test
    fun save_validForm_storesNormalisedValuesAndShowsMessage() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository)
        observe(viewModel)
        viewModel.onNameChanged("  Sam   Wilson ")
        viewModel.onEmailChanged(" sam@example.com ")
        viewModel.onPostcodeChanged("bs15tr")
        viewModel.onVehicleChanged("ib15 ptl")

        assertTrue(viewModel.save())

        assertEquals(
            UserProfile("Sam Wilson", "sam@example.com", "BS1 5TR", "IB15PTL"),
            repository.current
        )
        assertEquals(ProfileMessage.SAVED, viewModel.uiState.value.message)
    }

    @Test
    fun save_emptyForm_isValidAndSavesAnEmptyProfile() = runTest {
        val repository = FakeProfileRepository(saved)
        val viewModel = ProfileViewModel(repository)
        observe(viewModel)
        viewModel.startEditing()
        viewModel.onNameChanged("")
        viewModel.onEmailChanged("")
        viewModel.onPostcodeChanged("")
        viewModel.onVehicleChanged("  ")

        assertTrue(viewModel.save())

        assertTrue(repository.current.isEmpty)
    }

    @Test
    fun save_invalidFields_showsAnErrorNextToEachAndSavesNothing() = runTest {
        val repository = FakeProfileRepository()
        val viewModel = ProfileViewModel(repository)
        viewModel.onEmailChanged("sam@")
        viewModel.onPostcodeChanged("BS99")
        viewModel.onVehicleChanged("AB-12")

        assertFalse(viewModel.save())

        val form = viewModel.form.value
        assertNull(form.nameError)
        assertEquals(R.string.profile_error_email, form.emailError)
        assertEquals(R.string.profile_error_postcode, form.postcodeError)
        assertEquals(R.string.caz_error_invalid_characters, form.vehicleError)
        assertTrue(repository.current.isEmpty)
    }

    @Test
    fun typingInAField_clearsOnlyThatFieldsError() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository())
        viewModel.onEmailChanged("bad")
        viewModel.onPostcodeChanged("bad")
        viewModel.save()

        viewModel.onEmailChanged("sam@example.com")

        assertNull(viewModel.form.value.emailError)
        assertEquals(R.string.profile_error_postcode, viewModel.form.value.postcodeError)
    }

    @Test
    fun postcodeAndRegistration_areKeptAsTypedUntilSaved() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository())
        viewModel.onPostcodeChanged("bs1 5tr")
        viewModel.onVehicleChanged("ab12 cde")

        assertEquals("bs1 5tr", viewModel.form.value.postcode)
        assertEquals("ab12 cde", viewModel.form.value.vehicleRegistration)
    }

    @Test
    fun save_failure_showsFailureMessage() = runTest {
        val repository = FakeProfileRepository().apply { failWrites = true }
        val viewModel = ProfileViewModel(repository)
        observe(viewModel)
        viewModel.onNameChanged("Sam")

        viewModel.save()

        assertEquals(ProfileMessage.SAVE_FAILED, viewModel.uiState.value.message)
    }

    @Test
    fun clear_asksForConfirmationFirst() = runTest {
        val repository = FakeProfileRepository(saved)
        val viewModel = ProfileViewModel(repository)
        observe(viewModel)

        viewModel.onClearRequested()

        assertTrue(viewModel.uiState.value.showClearDialog)
        assertEquals(saved, repository.current)
    }

    @Test
    fun clear_cancelled_keepsTheProfile() = runTest {
        val repository = FakeProfileRepository(saved)
        val viewModel = ProfileViewModel(repository)
        observe(viewModel)

        viewModel.onClearRequested()
        viewModel.onClearDismissed()

        assertFalse(viewModel.uiState.value.showClearDialog)
        assertEquals(saved, repository.current)
    }

    @Test
    fun clear_confirmed_deletesTheProfile() = runTest {
        val repository = FakeProfileRepository(saved)
        val viewModel = ProfileViewModel(repository)
        observe(viewModel)

        viewModel.onClearRequested()
        viewModel.onClearConfirmed()

        assertTrue(repository.current.isEmpty)
        assertTrue(viewModel.uiState.value.profile!!.isEmpty)
        assertFalse(viewModel.uiState.value.showClearDialog)
        assertEquals(ProfileMessage.CLEARED, viewModel.uiState.value.message)
    }

    @Test
    fun messageShown_clearsTheMessage() = runTest {
        val viewModel = ProfileViewModel(FakeProfileRepository(saved))
        observe(viewModel)
        viewModel.onClearConfirmed()

        viewModel.onMessageShown()

        assertNull(viewModel.uiState.value.message)
    }
}
