package com.example.mobileapplication.data.model

import androidx.annotation.StringRes
import com.example.mobileapplication.R

/** Fictional prototype location. Coordinates and all information are illustrative only. */
data class CarPark(
    val id: String,
    @param:StringRes val nameRes: Int,
    val type: ParkingType,
    val area: ParkingArea,
    @param:StringRes val addressRes: Int,
    @param:StringRes val openingInformationRes: Int,
    @param:StringRes val priceNoteRes: Int,
    @param:StringRes val spacesNoteRes: Int,
    val latitude: Double,
    val longitude: Double,
    val category: ParkingCategory = ParkingCategory.SURFACE,
    val hasEvCharging: Boolean = false,
    val sampleDistanceMiles: Double = 0.4,
    val sampleSpaces: Int = 120,
    val sampleHourlyPrice: Double = 2.5
)

enum class ParkingCategory(@param:StringRes val labelRes: Int) {
    MULTI_STOREY(R.string.parking_multi_storey),
    SURFACE(R.string.parking_surface)
}

enum class ParkingType(@param:StringRes val labelRes: Int) {
    CAR_PARK(R.string.parking_type_car_park),
    PARK_AND_RIDE(R.string.parking_type_park_and_ride)
}

enum class ParkingArea(@param:StringRes val labelRes: Int) {
    CENTRE(R.string.parking_area_centre),
    NORTH(R.string.parking_area_north),
    SOUTH(R.string.parking_area_south),
    EAST(R.string.parking_area_east)
}
