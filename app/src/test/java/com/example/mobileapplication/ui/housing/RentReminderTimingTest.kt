package com.example.mobileapplication.ui.housing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/** Unit tests for [RentReminderTiming]. */
class RentReminderTimingTest {

    private val dueDate = LocalDate.of(2026, 12, 1)

    @Test
    fun reminderTime_isThreeDaysBeforeDueAtNineAm() {
        assertEquals(LocalDateTime.of(2026, 11, 28, 9, 0), RentReminderTiming.reminderTime(dueDate))
    }

    @Test
    fun reminderTime_crossesYearAndLeapDayCorrectly() {
        assertEquals(
            LocalDateTime.of(2026, 12, 29, 9, 0),
            RentReminderTiming.reminderTime(LocalDate.of(2027, 1, 1))
        )
        // 2028 is a leap year, so 3 days before 1 March is 27 February (29 Feb exists).
        assertEquals(
            LocalDateTime.of(2028, 2, 27, 9, 0),
            RentReminderTiming.reminderTime(LocalDate.of(2028, 3, 1))
        )
    }

    @Test
    fun delay_beforeReminderTime_isTimeLeftUntilReminder() {
        val now = LocalDateTime.of(2026, 11, 19, 9, 0)
        assertEquals(Duration.ofDays(9), RentReminderTiming.delayUntilReminder(now, dueDate))
    }

    @Test
    fun delay_exactlyAtReminderTime_isZero() {
        val now = LocalDateTime.of(2026, 11, 28, 9, 0)
        assertEquals(Duration.ZERO, RentReminderTiming.delayUntilReminder(now, dueDate))
    }

    @Test
    fun delay_reminderTimeAlreadyPassedButNotDueYet_firesStraightAway() {
        val now = LocalDateTime.of(2026, 11, 30, 18, 30)
        assertEquals(Duration.ZERO, RentReminderTiming.delayUntilReminder(now, dueDate))
    }

    @Test
    fun delay_onTheDueDate_isNull() {
        val now = LocalDateTime.of(2026, 12, 1, 0, 0)
        assertNull(RentReminderTiming.delayUntilReminder(now, dueDate))
    }

    @Test
    fun delay_dueDateAlreadyPassed_isNull() {
        val now = LocalDateTime.of(2026, 12, 5, 12, 0)
        assertNull(RentReminderTiming.delayUntilReminder(now, dueDate))
    }
}
