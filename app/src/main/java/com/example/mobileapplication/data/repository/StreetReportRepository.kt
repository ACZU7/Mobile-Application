package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.Locale

/** In-memory only. All data disappears when the process ends. */
class StreetReportRepository(private val today: () -> LocalDate = { LocalDate.now() }, seedSamples: Boolean = true) {
    private val records = MutableStateFlow(if (seedSamples) samples() else emptyList())
    val reports = records.asStateFlow()

    @Synchronized
    fun save(category: StreetCategory, description: String, locationLabel: String): StreetReport {
        val date = today()
        val sequence = records.value.filter { it.createdDate.year == date.year }
            .mapNotNull { it.reference.substringAfterLast('-').toIntOrNull() }.maxOrNull()?.plus(1) ?: 1
        val report = StreetReport(formatReference(date.year, sequence), category, description.trim(),
            locationLabel.trim(), 51.457, -2.592, createdDate = date, status = StreetStatus.SUBMITTED)
        records.value = listOf(report) + records.value
        return report
    }
    fun get(reference: String) = records.value.find { it.reference == reference }

    companion object {
        val shared by lazy { StreetReportRepository() }
        fun formatReference(year: Int, sequence: Int): String = String.format(Locale.ROOT, "SR-%d-%04d", year, sequence)
        private fun samples() = listOf(
            StreetReport(formatReference(2026, 3), StreetCategory.POTHOLE, "Fictional pothole near a sample crossing.", "Demo Nelson Street, Bristol", 51.457, -2.592, createdDate = LocalDate.of(2026, 10, 20), status = StreetStatus.IN_PROGRESS),
            StreetReport(formatReference(2026, 2), StreetCategory.STREET_LIGHT, "Fictional street light awaiting inspection.", "Demo Park Street, Bristol", 51.457, -2.592, createdDate = LocalDate.of(2026, 10, 18), status = StreetStatus.SUBMITTED)
        )
    }
}
