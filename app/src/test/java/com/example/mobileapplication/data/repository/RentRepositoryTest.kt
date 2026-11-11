package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.PaymentMethod
import com.example.mobileapplication.data.model.RentAccount
import com.example.mobileapplication.data.model.UpcomingRentPayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Unit tests for [RentRepository] and the [RentAccount] model. */
class RentRepositoryTest {

    private val today = LocalDate.of(2026, 11, 11)
    private val account = RentRepository(today = { today }).getAccount()

    @Test
    fun account_isLabelledAsSampleAndUpToDate() {
        assertTrue(account.isSample)
        assertTrue(account.isUpToDate)
        assertEquals(0L, account.amountOwedPence)
    }

    @Test
    fun nextPayment_isFirstOfNextMonthForMonthlyRent() {
        assertEquals(LocalDate.of(2026, 12, 1), account.nextPayment.dueDate)
        assertEquals(52_000L, account.nextPayment.amountPence)
    }

    @Test
    fun history_hasSixMonthlyPaymentsNewestFirst() {
        val dates = account.paymentHistory.map { it.date }
        assertEquals(
            listOf(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 6, 1)
            ),
            dates
        )
        assertTrue(account.paymentHistory.all { it.amountPence == RentRepository.MONTHLY_RENT_PENCE })
    }

    @Test
    fun history_isNeverInTheFuture() {
        val firstOfMonth = RentRepository(today = { LocalDate.of(2026, 1, 1) }).getAccount()
        assertTrue(firstOfMonth.paymentHistory.all { !it.date.isAfter(LocalDate.of(2026, 1, 1)) })
        assertEquals(LocalDate.of(2026, 2, 1), firstOfMonth.nextPayment.dueDate)
    }

    @Test
    fun history_includesMoreThanOnePaymentMethod() {
        val methods = account.paymentHistory.map { it.method }.toSet()
        assertTrue(PaymentMethod.DIRECT_DEBIT in methods)
        assertTrue(methods.size > 1)
    }

    @Test
    fun sampleAccount_withAmountOwed_isNotUpToDate() {
        val owing = RentRepository.sampleAccount(today, amountOwedPence = 12_000L)
        assertFalse(owing.isUpToDate)
        assertEquals(12_000L, owing.amountOwedPence)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rentAccount_rejectsNegativeAmountOwed() {
        RentAccount(
            amountOwedPence = -1L,
            nextPayment = UpcomingRentPayment(today, 0L),
            paymentHistory = emptyList()
        )
    }
}
