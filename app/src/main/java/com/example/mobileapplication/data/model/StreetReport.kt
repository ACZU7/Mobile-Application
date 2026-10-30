package com.example.mobileapplication.data.model

import com.example.mobileapplication.R
import java.time.LocalDate

enum class StreetCategory(val labelRes: Int) {
    POTHOLE(R.string.street_pothole), STREET_LIGHT(R.string.street_light),
    FLY_TIPPING(R.string.street_fly_tipping), GRAFFITI(R.string.street_graffiti)
}
enum class StreetStatus(val labelRes: Int) {
    SUBMITTED(R.string.street_submitted), IN_PROGRESS(R.string.street_in_progress), FIXED(R.string.street_fixed)
}
/** Every record is fictional prototype data; coordinates are illustrative, never GPS. */
data class StreetReport(
    val reference: String,
    val category: StreetCategory,
    val description: String,
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val imageUri: String? = null,
    val createdDate: LocalDate,
    val status: StreetStatus
)
