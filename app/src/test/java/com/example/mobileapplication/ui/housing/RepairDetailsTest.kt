package com.example.mobileapplication.ui.housing

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Unit tests for the date formatting used on the repair details card. */
class RepairDetailsTest {

    @Test
    fun formatDate_usesUkStyle() {
        assertEquals("22 Oct 2026", formatDate(LocalDate.of(2026, 10, 22)))
    }

    @Test
    fun formatDate_doesNotPadSingleDigitDays() {
        assertEquals("1 Mar 2026", formatDate(LocalDate.of(2026, 3, 1)))
    }
}
