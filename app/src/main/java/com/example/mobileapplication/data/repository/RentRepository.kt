package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.PaymentMethod
import com.example.mobileapplication.data.model.RentAccount
import com.example.mobileapplication.data.model.RentPayment
import com.example.mobileapplication.data.model.UpcomingRentPayment
import java.time.LocalDate

/**
 * Provides the resident's rent account.
 *
 * **All of this is sample data.** The prototype does not connect to a real
 * Council rent account and never takes payments. The dates are worked out from
 * [today], so the sample always looks current: rent of £520.00 is due on the
 * 1st of every month, the last six months have been paid, and nothing is overdue.
 *
 * @param today Gives the current date. Tests pass a fixed date so the results are predictable.
 */
class RentRepository(
    private val today: () -> LocalDate = { LocalDate.now() }
) {

    /** Returns the sample rent account as it looks today. */
    fun getAccount(): RentAccount = sampleAccount(today())

    /** Holds the sample values and the function that builds the sample account. */
    companion object {

        /** The sample monthly rent: £520.00, in pence. */
        const val MONTHLY_RENT_PENCE = 52_000L

        /** How many months of payment history the sample shows. */
        const val HISTORY_MONTHS = 6

        /**
         * Builds a sample account relative to [today].
         *
         * The next payment is the 1st of next month, and the history holds the
         * 1st of each of the previous [HISTORY_MONTHS] months, newest first.
         *
         * @param amountOwedPence The overdue amount to show. Zero (the default) means up to date;
         * previews use a positive value to show the "amount owed" state.
         */
        fun sampleAccount(today: LocalDate, amountOwedPence: Long = 0L): RentAccount {
            val firstOfThisMonth = today.withDayOfMonth(1)
            val history = (0 until HISTORY_MONTHS).map { monthsAgo ->
                RentPayment(
                    date = firstOfThisMonth.minusMonths(monthsAgo.toLong()),
                    amountPence = MONTHLY_RENT_PENCE,
                    // One card payment, so the history shows more than one method.
                    method = if (monthsAgo == 3) PaymentMethod.DEBIT_CARD else PaymentMethod.DIRECT_DEBIT
                )
            }
            return RentAccount(
                amountOwedPence = amountOwedPence,
                nextPayment = UpcomingRentPayment(
                    dueDate = firstOfThisMonth.plusMonths(1),
                    amountPence = MONTHLY_RENT_PENCE
                ),
                paymentHistory = history,
                isSample = true
            )
        }
    }
}
