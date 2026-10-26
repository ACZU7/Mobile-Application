package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** Unit tests for [RepairRepository] and its reference generator. */
class RepairRepositoryTest {

    private val fixedDate = LocalDate.of(2026, 10, 21)
    private val repository = RepairRepository(today = { fixedDate })

    private fun addSample() = repository.addRequest(
        room = Room.KITCHEN,
        issueType = IssueType.LEAK,
        description = "Tap is leaking constantly",
        appointmentPreference = AppointmentPreference.MORNING
    )

    @Test
    fun formatReference_padsSequenceToFourDigits() {
        assertEquals("HR-2026-0004", RepairRepository.formatReference(2026, 4))
        assertEquals("HR-2027-0123", RepairRepository.formatReference(2027, 123))
        assertEquals("HR-2026-9999", RepairRepository.formatReference(2026, 9999))
    }

    @Test
    fun repository_startsWithThreeSampleRequests() {
        assertEquals(3, repository.requests.value.size)
    }

    @Test
    fun addRequest_firstNewRequestGetsReference0004() {
        assertEquals("HR-2026-0004", addSample().reference)
    }

    @Test
    fun addRequest_consecutiveRequestsIncrementReference() {
        addSample()
        assertEquals("HR-2026-0005", addSample().reference)
    }

    @Test
    fun addRequest_usesYearFromToday() {
        val nextYearRepository = RepairRepository(today = { LocalDate.of(2027, 1, 5) })
        val request = nextYearRepository.addRequest(
            Room.HALLWAY, IssueType.ELECTRICAL, "Light switch sparks", AppointmentPreference.ANY_TIME
        )
        assertEquals("HR-2027-0004", request.reference)
    }

    @Test
    fun addRequest_savesRequestWithSubmittedStatusAndDate() {
        val added = addSample()

        assertEquals(4, repository.requests.value.size)
        assertEquals(added, repository.requests.value.last())
        assertEquals(RepairStatus.SUBMITTED, added.status)
        assertEquals(fixedDate, added.createdDate)
    }

    @Test
    fun getRequest_findsSavedRequestOrReturnsNull() {
        val added = addSample()

        assertEquals(added, repository.getRequest("HR-2026-0004"))
        assertNull(repository.getRequest("HR-2026-9999"))
    }
}
