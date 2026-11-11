package com.example.mobileapplication.ui.housing

import com.example.mobileapplication.data.repository.RentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Unit tests for [RentViewModel] and the rent formatting helpers. */
class RentViewModelTest {

    private val today = LocalDate.of(2026, 11, 11)
    private val viewModel = RentViewModel(RentRepository(today = { today }))

    @Test
    fun initialState_showsSampleAccountWithoutPaymentMessage() {
        val state = viewModel.uiState.value

        assertEquals(RentRepository.sampleAccount(today), state.account)
        assertTrue(state.account.isSample)
        assertFalse(state.showPaymentUnavailable)
    }

    @Test
    fun payRent_onlyShowsUnavailableMessageAndLeavesAccountUnchanged() {
        val before = viewModel.uiState.value.account

        viewModel.onPayRentClicked()

        val state = viewModel.uiState.value
        assertTrue(state.showPaymentUnavailable)
        assertEquals(before, state.account)
    }

    @Test
    fun messageShown_hidesTheMessage() {
        viewModel.onPayRentClicked()
        viewModel.onPaymentMessageShown()
        assertFalse(viewModel.uiState.value.showPaymentUnavailable)
    }

    @Test
    fun payRentTwice_stillShowsTheMessageAgain() {
        viewModel.onPayRentClicked()
        viewModel.onPaymentMessageShown()
        viewModel.onPayRentClicked()
        assertTrue(viewModel.uiState.value.showPaymentUnavailable)
    }

    @Test
    fun formatPence_showsPoundsAndPence() {
        assertEquals("£520.00", formatPence(52_000L))
        assertEquals("£0.00", formatPence(0L))
        assertEquals("£1,234.05", formatPence(123_405L))
    }

    @Test
    fun formatLongDate_writesMonthInFull() {
        assertEquals("1 December 2026", formatLongDate(LocalDate.of(2026, 12, 1)))
    }
}
