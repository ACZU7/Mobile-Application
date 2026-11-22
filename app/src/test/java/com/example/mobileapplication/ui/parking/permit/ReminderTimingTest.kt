package com.example.mobileapplication.ui.parking.permit

import com.example.mobileapplication.data.repository.SamplePermit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderTimingTest {
    private val expiry = LocalDate.of(2027, 3, 31)

    @Test
    fun reminderIsFourteenDaysBeforeExpiryAtNineAm() {
        assertEquals(LocalDateTime.of(2027, 3, 17, 9, 0), ReminderTiming.reminderTime(expiry))
    }

    @Test
    fun delay_isTimeUntilTheReminder() {
        val now = LocalDateTime.of(2027, 3, 16, 9, 0)
        assertEquals(Duration.ofDays(1), ReminderTiming.delayUntilReminder(now, expiry))
    }

    @Test
    fun reminderTimeAlreadyPassedButPermitValid_firesStraightAway() {
        val now = LocalDateTime.of(2027, 3, 30, 18, 0)
        assertEquals(Duration.ZERO, ReminderTiming.delayUntilReminder(now, expiry))
    }

    @Test
    fun lastDayOfPermit_stillFires() {
        assertEquals(Duration.ZERO, ReminderTiming.delayUntilReminder(LocalDateTime.of(2027, 3, 31, 23, 0), expiry))
    }

    @Test
    fun expiredPermit_hasNoReminder() {
        assertNull(ReminderTiming.delayUntilReminder(LocalDateTime.of(2027, 4, 1, 0, 1), expiry))
    }

    @Test
    fun permitExpiry_isInclusiveOfTheExpiryDate() {
        val permit = SamplePermit.permit
        assertFalse(permit.isExpired(permit.expiryDate))
        assertTrue(permit.isExpired(permit.expiryDate.plusDays(1)))
    }

    @Test
    fun samplePermit_isClearlyFictionalAndInTheSampleZone() {
        assertEquals(SamplePermit.zone.code, SamplePermit.permit.zoneCode)
        assertTrue(SamplePermit.permit.reference.startsWith("RP-"))
    }
}
