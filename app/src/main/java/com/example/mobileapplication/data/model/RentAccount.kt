package com.example.mobileapplication.data.model

import androidx.annotation.StringRes
import com.example.mobileapplication.R
import java.time.LocalDate

/**
 * A resident's rent account.
 *
 * This is **sample data only**. The prototype does not connect to any Bristol City
 * Council rent system, so the balance and payments are made up and must always
 * be labelled as sample data on screen.
 *
 * Money is stored in whole pence (`Long`) rather than a floating-point number,
 * so amounts such as £520.00 are always exact.
 *
 * @property amountOwedPence How much rent is overdue, in pence. Zero means the account is up to date.
 * @property nextPayment The next rent payment that is due.
 * @property paymentHistory Past payments, newest first.
 * @property isSample Always true in this prototype; kept so screens can check it explicitly.
 */
data class RentAccount(
    val amountOwedPence: Long,
    val nextPayment: UpcomingRentPayment,
    val paymentHistory: List<RentPayment>,
    val isSample: Boolean = true
) {
    init {
        require(amountOwedPence >= 0) { "The amount owed cannot be negative." }
    }

    /** True when nothing is overdue, so the screen can say "You're up to date". */
    val isUpToDate: Boolean get() = amountOwedPence == 0L
}

/**
 * A rent payment that is due in the future.
 *
 * @property dueDate The date the payment is due.
 * @property amountPence The amount due, in pence.
 */
data class UpcomingRentPayment(
    val dueDate: LocalDate,
    val amountPence: Long
)

/**
 * A rent payment that has already been made (sample data).
 *
 * @property date The date the payment was received.
 * @property amountPence The amount paid, in pence.
 * @property method How it was paid.
 */
data class RentPayment(
    val date: LocalDate,
    val amountPence: Long,
    val method: PaymentMethod
)

/**
 * How a sample rent payment was made.
 *
 * [labelRes] points to the readable label in `strings.xml`.
 */
enum class PaymentMethod(@StringRes val labelRes: Int) {
    DIRECT_DEBIT(R.string.rent_method_direct_debit),
    DEBIT_CARD(R.string.rent_method_debit_card),
    BANK_TRANSFER(R.string.rent_method_bank_transfer)
}
