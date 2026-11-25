package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.*
import android.content.Context
import com.example.mobileapplication.data.local.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.Locale

/** Production uses the shared Room database; tests can use an isolated in-memory repository. */
class StreetReportRepository(private val today: () -> LocalDate = { LocalDate.now() },
    private val seedSamples: Boolean = true, private val dao: StreetReportDao? = null) {
    private val records = MutableStateFlow(if (dao == null && seedSamples) samples() else emptyList())
    val reports = records.asStateFlow()
    val persistent get() = dao != null
    private val writeLock = Mutex()
    private var loaded = dao == null

    suspend fun load() = writeLock.withLock {
        if (!loaded) {
            var rows = dao!!.getAll()
            if (rows.isEmpty() && seedSamples) {
                dao.insertSamples(samples().map { it.toEntity() })
                rows = dao.getAll()
            }
            records.value = rows.map { it.toModel() }
            loaded = true
        }
    }

    suspend fun save(category: StreetCategory, description: String, locationLabel: String, imageUris: List<String> = emptyList()): StreetReport {
        load()
        return writeLock.withLock {
            val date = today()
            val sequence = records.value.filter { it.createdDate.year == date.year }
                .mapNotNull { it.reference.substringAfterLast('-').toIntOrNull() }.maxOrNull()?.plus(1) ?: 1
            val report = StreetReport(formatReference(date.year, sequence), category, description.trim(),
                locationLabel.trim(), SAMPLE_LATITUDE, SAMPLE_LONGITUDE, imageUris = imageUris, createdDate = date, status = StreetStatus.SUBMITTED)
            dao?.insert(report.toEntity())
            records.value = listOf(report) + records.value
            report
        }
    }
    fun get(reference: String) = records.value.find { it.reference == reference }

    /** Publish the new status only after storage succeeds. Resident and staff screens share this flow. */
    suspend fun updateStatus(reference: String, status: StreetStatus) {
        load()
        writeLock.withLock {
            requireNotNull(get(reference)) { "Unknown report" }
            if (dao != null) check(dao.updateStatus(reference, status.name) == 1) { "Report was not updated" }
            records.value = records.value.map { if (it.reference == reference) it.copy(status = status) else it }
        }
    }

    companion object {
        @Volatile private var instance: StreetReportRepository? = null
        fun getInstance(context: Context): StreetReportRepository = instance ?: synchronized(this) {
            instance ?: StreetReportRepository(dao = AppDatabase.getInstance(context).streetReportDao())
                .also { instance = it }
        }
        /** Fixed sample position used for new reports and shown on the form map (near Nelson Street). Not GPS. */
        const val SAMPLE_LATITUDE = 51.4566
        const val SAMPLE_LONGITUDE = -2.5937
        fun formatReference(year: Int, sequence: Int): String = String.format(Locale.ROOT, "SR-%d-%04d", year, sequence)
        private fun samples() = listOf(
            StreetReport(formatReference(2026, 3), StreetCategory.POTHOLE, "Fictional pothole near a sample crossing.", "Demo Nelson Street, Bristol", 51.457, -2.592, createdDate = LocalDate.of(2026, 10, 20), status = StreetStatus.IN_PROGRESS),
            StreetReport(formatReference(2026, 2), StreetCategory.STREET_LIGHT, "Fictional street light awaiting inspection.", "Demo Park Street, Bristol", 51.457, -2.592, createdDate = LocalDate.of(2026, 10, 18), status = StreetStatus.SUBMITTED)
        )
    }
}
