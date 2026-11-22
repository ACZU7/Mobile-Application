package com.example.mobileapplication.data.repository

import android.content.Context
import com.example.mobileapplication.data.local.AppDatabase
import com.example.mobileapplication.data.local.RepairRequestDao
import com.example.mobileapplication.data.local.toEntity
import com.example.mobileapplication.data.local.toModel
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/**
 * Stores housing repair requests on the phone using Room, so they are still
 * there after the app is closed and reopened.
 *
 * The first time it is used on an empty table it inserts three sample requests
 * (marked [RepairRequest.isSample]) so the screens have something to show.
 * Everything is demonstration data: nothing is sent to Bristol City Council.
 *
 * @param dao Database access. Unit tests pass an in-memory fake, so they don't need a device.
 * @param today Gives the current date. Tests can pass a fixed date so the
 * generated references are predictable.
 */
class RepairRepository(
    private val dao: RepairRequestDao,
    private val today: () -> LocalDate = { LocalDate.now() }
) {

    /**
     * Makes seeding and reference numbering happen one at a time, so two quick
     * submits can never be given the same reference.
     */
    private val writeLock = Mutex()

    /** Set once the table is known to contain data, so seeding is only checked once. */
    @Volatile
    private var seeded = false

    /**
     * All repair requests, newest first.
     *
     * Collecting it seeds the sample data if needed, then emits a fresh list
     * every time a request is added.
     */
    val requests: Flow<List<RepairRequest>> = flow {
        seedIfEmpty()
        emitAll(dao.observeAll().map { rows -> rows.map { it.toModel() } })
    }

    /**
     * Creates a new repair request with the next reference number, saves it in
     * the database and returns it so the caller can show the reference to the user.
     *
     * New requests always start with the [RepairStatus.SUBMITTED] status.
     */
    suspend fun addRequest(
        room: Room,
        issueType: IssueType,
        description: String,
        appointmentPreference: AppointmentPreference
    ): RepairRequest {
        seedIfEmpty()
        return writeLock.withLock {
            val date = today()
            val request = RepairRequest(
                reference = formatReference(date.year, dao.count() + 1),
                room = room,
                issueType = issueType,
                description = description,
                appointmentPreference = appointmentPreference,
                createdDate = date,
                status = RepairStatus.SUBMITTED
            )
            dao.insert(request.toEntity())
            request
        }
    }

    /** Returns the request with this [reference], or null if there isn't one. */
    suspend fun getRequest(reference: String): RepairRequest? {
        seedIfEmpty()
        return dao.findByReference(reference)?.toModel()
    }

    /**
     * Inserts the three sample requests, but only when the table is empty, so
     * the user's own requests are never mixed up with fresh copies of the samples.
     */
    suspend fun seedIfEmpty() {
        if (seeded) return
        writeLock.withLock {
            if (!seeded) {
                if (dao.count() == 0) {
                    dao.insertAll(sampleRequests().map { it.toEntity() })
                }
                seeded = true
            }
        }
    }

    /** Holds the shared app-wide instance and the reference formatter. */
    companion object {

        @Volatile
        private var instance: RepairRepository? = null

        /**
         * Returns the repository shared by the whole app, so every screen sees the
         * same requests. (Later this could be provided by dependency injection instead.)
         */
        fun getInstance(context: Context): RepairRepository =
            instance ?: synchronized(this) {
                instance ?: RepairRepository(
                    AppDatabase.getInstance(context).repairRequestDao()
                ).also { instance = it }
            }

        /**
         * Builds a reference such as "HR-2026-0004".
         *
         * "HR" means Housing Repair. The [sequence] is padded with zeros to four digits.
         * This is a pure function (same input, same output), which makes it easy to unit test.
         */
        fun formatReference(year: Int, sequence: Int): String =
            "HR-%d-%04d".format(year, sequence)

        /** Three fictional requests used as demonstration data. */
        fun sampleRequests(): List<RepairRequest> = listOf(
            RepairRequest(
                reference = formatReference(2026, 1),
                room = Room.BATHROOM,
                issueType = IssueType.LEAK,
                description = "Water dripping from under the bathroom sink.",
                appointmentPreference = AppointmentPreference.MORNING,
                createdDate = LocalDate.of(2026, 9, 14),
                status = RepairStatus.COMPLETED,
                isSample = true
            ),
            RepairRequest(
                reference = formatReference(2026, 2),
                room = Room.BEDROOM,
                issueType = IssueType.DAMP_MOULD,
                description = "Black mould spreading on the wall by the window.",
                appointmentPreference = AppointmentPreference.ANY_TIME,
                createdDate = LocalDate.of(2026, 10, 2),
                status = RepairStatus.APPOINTMENT_BOOKED,
                isSample = true
            ),
            RepairRequest(
                reference = formatReference(2026, 3),
                room = Room.KITCHEN,
                issueType = IssueType.HEATING,
                description = "Boiler shows an error code and there is no hot water.",
                appointmentPreference = AppointmentPreference.AFTERNOON,
                createdDate = LocalDate.of(2026, 10, 15),
                status = RepairStatus.IN_PROGRESS,
                isSample = true
            )
        )
    }
}
