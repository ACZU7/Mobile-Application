package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/**
 * Stores housing repair requests in memory (no database yet).
 *
 * It starts with three sample requests so the progress screen has something to show.
 * The data is lost when the app closes; Room persistence is planned for Phase 3.
 *
 * @param today Gives the current date. Tests can pass a fixed date so the
 * generated references are predictable.
 */
class RepairRepository(
    private val today: () -> LocalDate = { LocalDate.now() }
) {

    private val _requests = MutableStateFlow(sampleRequests())

    /**
     * All repair requests, newest last.
     *
     * It is a [StateFlow], so a screen observing it updates automatically when
     * a new request is added.
     */
    val requests: StateFlow<List<RepairRequest>> = _requests.asStateFlow()

    /**
     * Creates a new repair request with the next reference number, saves it in
     * memory and returns it so the caller can show the reference to the user.
     *
     * New requests always start with the [RepairStatus.SUBMITTED] status.
     */
    fun addRequest(
        room: Room,
        issueType: IssueType,
        description: String,
        appointmentPreference: AppointmentPreference
    ): RepairRequest {
        val date = today()
        val request = RepairRequest(
            reference = formatReference(date.year, _requests.value.size + 1),
            room = room,
            issueType = issueType,
            description = description,
            appointmentPreference = appointmentPreference,
            createdDate = date,
            status = RepairStatus.SUBMITTED
        )
        _requests.update { it + request }
        return request
    }

    /** Returns the request with this [reference], or null if there isn't one. */
    fun getRequest(reference: String): RepairRequest? =
        _requests.value.find { it.reference == reference }

    /** Holds the shared app-wide instance and the reference formatter. */
    companion object {

        /**
         * One repository shared by the whole app, so every screen sees the same requests.
         * (Later this could be provided by dependency injection instead.)
         */
        val shared: RepairRepository by lazy { RepairRepository() }

        /**
         * Builds a reference such as "HR-2026-0004".
         *
         * "HR" means Housing Repair. The [sequence] is padded with zeros to four digits.
         * This is a pure function (same input, same output), which makes it easy to unit test.
         */
        fun formatReference(year: Int, sequence: Int): String =
            "HR-%d-%04d".format(year, sequence)

        /** Three fictional requests used as demonstration data. */
        private fun sampleRequests(): List<RepairRequest> = listOf(
            RepairRequest(
                reference = formatReference(2026, 1),
                room = Room.BATHROOM,
                issueType = IssueType.LEAK,
                description = "Water dripping from under the bathroom sink.",
                appointmentPreference = AppointmentPreference.MORNING,
                createdDate = LocalDate.of(2026, 9, 14),
                status = RepairStatus.COMPLETED
            ),
            RepairRequest(
                reference = formatReference(2026, 2),
                room = Room.BEDROOM,
                issueType = IssueType.DAMP_MOULD,
                description = "Black mould spreading on the wall by the window.",
                appointmentPreference = AppointmentPreference.ANY_TIME,
                createdDate = LocalDate.of(2026, 10, 2),
                status = RepairStatus.APPOINTMENT_BOOKED
            ),
            RepairRequest(
                reference = formatReference(2026, 3),
                room = Room.KITCHEN,
                issueType = IssueType.HEATING,
                description = "Boiler shows an error code and there is no hot water.",
                appointmentPreference = AppointmentPreference.AFTERNOON,
                createdDate = LocalDate.of(2026, 10, 15),
                status = RepairStatus.IN_PROGRESS
            )
        )
    }
}
