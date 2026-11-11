package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.local.toEntity
import com.example.mobileapplication.data.local.toModel
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.testutil.FakeRepairRequestDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Unit tests for [RepairRepository], its seeding and its reference generator. */
class RepairRepositoryTest {

    private val fixedDate = LocalDate.of(2026, 10, 21)
    private val dao = FakeRepairRequestDao()
    private val repository = RepairRepository(dao, today = { fixedDate })

    private suspend fun addSample() = repository.addRequest(
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
    fun emptyDatabase_isSeededWithThreeLabelledSampleRequests() = runTest {
        val requests = repository.requests.first()

        assertEquals(3, requests.size)
        assertTrue(requests.all { it.isSample })
    }

    @Test
    fun seeding_doesNothingWhenTableAlreadyHasData() = runTest {
        val own = RepairRequest(
            reference = "HR-2026-0001",
            room = Room.HALLWAY,
            issueType = IssueType.ELECTRICAL,
            description = "Hall light flickers",
            appointmentPreference = AppointmentPreference.ANY_TIME,
            createdDate = fixedDate,
            status = RepairStatus.SUBMITTED
        )
        val existingDao = FakeRepairRequestDao(listOf(own.toEntity()))
        val existingRepository = RepairRepository(existingDao, today = { fixedDate })

        val requests = existingRepository.requests.first()

        assertEquals(listOf(own), requests)
    }

    @Test
    fun seeding_happensOnlyOnceEvenIfCalledAgain() = runTest {
        repository.seedIfEmpty()
        repository.seedIfEmpty()
        assertEquals(3, dao.stored.size)
    }

    @Test
    fun requests_areNewestFirst() = runTest {
        val dates = repository.requests.first().map { it.createdDate }
        assertEquals(dates.sortedDescending(), dates)
    }

    @Test
    fun addRequest_firstNewRequestGetsReference0004() = runTest {
        assertEquals("HR-2026-0004", addSample().reference)
    }

    @Test
    fun addRequest_consecutiveRequestsIncrementReference() = runTest {
        addSample()
        assertEquals("HR-2026-0005", addSample().reference)
    }

    @Test
    fun addRequest_usesYearFromToday() = runTest {
        val nextYearRepository = RepairRepository(FakeRepairRequestDao(), today = { LocalDate.of(2027, 1, 5) })
        val request = nextYearRepository.addRequest(
            Room.HALLWAY, IssueType.ELECTRICAL, "Light switch sparks", AppointmentPreference.ANY_TIME
        )
        assertEquals("HR-2027-0004", request.reference)
    }

    @Test
    fun addRequest_savesRequestWithSubmittedStatusAndDate() = runTest {
        val added = addSample()

        val requests = repository.requests.first()
        assertEquals(4, requests.size)
        assertEquals(added, requests.first())
        assertEquals(RepairStatus.SUBMITTED, added.status)
        assertEquals(fixedDate, added.createdDate)
        assertFalse(added.isSample)
    }

    @Test
    fun addRequest_isStillThereForANewRepositoryOnTheSameDatabase() = runTest {
        val added = addSample()

        // A new repository over the same stored rows is what happens after an app restart.
        val afterRestart = RepairRepository(dao, today = { fixedDate })

        assertEquals(added, afterRestart.getRequest(added.reference))
        assertEquals(4, afterRestart.requests.first().size)
    }

    @Test
    fun getRequest_findsSavedRequestOrReturnsNull() = runTest {
        val added = addSample()

        assertEquals(added, repository.getRequest("HR-2026-0004"))
        assertNull(repository.getRequest("HR-2026-9999"))
    }

    @Test
    fun entityConversion_roundTripsEveryField() {
        val request = RepairRequest(
            reference = "HR-2026-0042",
            room = Room.LIVING_ROOM,
            issueType = IssueType.DOOR_WINDOW,
            description = "Window will not close",
            appointmentPreference = AppointmentPreference.AFTERNOON,
            createdDate = LocalDate.of(2026, 2, 28),
            status = RepairStatus.IN_PROGRESS,
            imageUri = "content://photo/1",
            isSample = true
        )
        assertEquals(request, request.toEntity().toModel())
    }
}
