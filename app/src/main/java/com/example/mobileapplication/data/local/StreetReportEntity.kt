package com.example.mobileapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mobileapplication.data.model.*
import java.time.LocalDate
import java.util.Base64

@Entity(tableName = "street_reports")
data class StreetReportEntity(
    @PrimaryKey val reference: String,
    val category: String,
    val description: String,
    val locationLabel: String,
    val latitude: Double,
    val longitude: Double,
    val photos: String,
    val createdEpochDay: Long,
    val status: String
)

// Base64 keeps separators in file names from changing the stored photo list.
fun StreetReport.toEntity() = StreetReportEntity(reference, category.name, description, locationLabel,
    latitude, longitude, imageUris.joinToString("\n") {
        Base64.getEncoder().encodeToString(it.toByteArray(Charsets.UTF_8))
    }, createdDate.toEpochDay(), status.name)

fun StreetReportEntity.toModel() = StreetReport(reference, StreetCategory.valueOf(category), description,
    locationLabel, latitude, longitude, if (photos.isEmpty()) emptyList() else photos.split('\n').map {
        String(Base64.getDecoder().decode(it), Charsets.UTF_8)
    }, LocalDate.ofEpochDay(createdEpochDay), StreetStatus.valueOf(status))
