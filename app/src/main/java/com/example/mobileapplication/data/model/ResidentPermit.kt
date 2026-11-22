package com.example.mobileapplication.data.model

import androidx.annotation.StringRes
import java.time.LocalDate

/** A fictional residents' parking zone. Not real Council zone data. */
data class ParkingZone(
    val code: String,
    @param:StringRes val hoursRes: Int,
    @param:StringRes val rulesRes: Int
)

/** A fictional resident's permit, stored only on this device. */
data class ResidentPermit(
    val zoneCode: String,
    val reference: String,
    val vehicleRegistration: String,
    val expiryDate: LocalDate
) {
    /** The permit is valid until the end of its expiry date. */
    fun isExpired(today: LocalDate): Boolean = today.isAfter(expiryDate)
}
