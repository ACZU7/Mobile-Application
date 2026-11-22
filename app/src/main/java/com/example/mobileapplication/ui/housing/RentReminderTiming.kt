package com.example.mobileapplication.ui.housing

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Works out when the "rent is due soon" reminder should fire.
 *
 * Plain Kotlin with no Android types, so it can be unit tested on its own.
 * The reminder is shown at [REMINDER_HOUR]:00 on the day that is
 * [DAYS_BEFORE_DUE] days before the payment is due.
 */
object RentReminderTiming {

    /** How many days before the due date the reminder is shown. */
    const val DAYS_BEFORE_DUE = 3L

    /** The hour of the day (24-hour clock) the reminder is shown. */
    const val REMINDER_HOUR = 9

    /** The date and time the reminder for a payment due on [dueDate] should be shown. */
    fun reminderTime(dueDate: LocalDate): LocalDateTime =
        dueDate.minusDays(DAYS_BEFORE_DUE).atTime(REMINDER_HOUR, 0)

    /**
     * How long to wait from [now] before showing the reminder.
     *
     * - Before the reminder time: the time left until then.
     * - After the reminder time but before the due date: zero, so it is shown straight away.
     * - On or after the due date: null, because it is too late to remind the user.
     */
    fun delayUntilReminder(now: LocalDateTime, dueDate: LocalDate): Duration? {
        if (!now.toLocalDate().isBefore(dueDate)) return null
        val delay = Duration.between(now, reminderTime(dueDate))
        return if (delay.isNegative) Duration.ZERO else delay
    }
}
