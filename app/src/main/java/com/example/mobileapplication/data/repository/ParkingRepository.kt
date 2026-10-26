package com.example.mobileapplication.data.repository

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.CarPark
import com.example.mobileapplication.data.model.ParkingCategory
import com.example.mobileapplication.data.model.ParkingArea
import com.example.mobileapplication.data.model.ParkingType

/** Fixed fictional Bristol examples, held in memory. No live Council data or services. */
class ParkingRepository {
    val carParks: List<CarPark> = listOf(
        sample("sample-01", R.string.parking_name_01, ParkingType.CAR_PARK, ParkingArea.CENTRE, R.string.parking_address_01, 51.454, -2.593),
        sample("sample-02", R.string.parking_name_02, ParkingType.CAR_PARK, ParkingArea.CENTRE, R.string.parking_address_02, 51.452, -2.599),
        sample("sample-03", R.string.parking_name_03, ParkingType.CAR_PARK, ParkingArea.NORTH, R.string.parking_address_03, 51.49, -2.59),
        sample("sample-04", R.string.parking_name_04, ParkingType.CAR_PARK, ParkingArea.SOUTH, R.string.parking_address_04, 51.43, -2.60),
        sample("sample-05", R.string.parking_name_05, ParkingType.CAR_PARK, ParkingArea.EAST, R.string.parking_address_05, 51.46, -2.55),
        sample("sample-06", R.string.parking_name_06, ParkingType.PARK_AND_RIDE, ParkingArea.NORTH, R.string.parking_address_06, 51.51, -2.60),
        sample("sample-07", R.string.parking_name_07, ParkingType.PARK_AND_RIDE, ParkingArea.SOUTH, R.string.parking_address_07, 51.41, -2.61),
        sample("sample-08", R.string.parking_name_08, ParkingType.PARK_AND_RIDE, ParkingArea.EAST, R.string.parking_address_08, 51.47, -2.52)
    )

    fun getCarPark(id: String): CarPark? = carParks.find { it.id == id }

    private fun sample(
        id: String, name: Int, type: ParkingType, area: ParkingArea,
        address: Int, latitude: Double, longitude: Double
    ) = CarPark(
        id, name, type, area, address,
        R.string.parking_sample_opening,
        if (type == ParkingType.CAR_PARK) R.string.parking_sample_price else R.string.parking_sample_ride_price,
        R.string.parking_sample_spaces, latitude, longitude,
        category = if (id in listOf("sample-01", "sample-02", "sample-03")) ParkingCategory.MULTI_STOREY else ParkingCategory.SURFACE,
        hasEvCharging = id in listOf("sample-01", "sample-04", "sample-06"),
        sampleDistanceMiles = listOf(0.4, 0.6, 0.8, 1.2, 1.5, 2.0, 2.4, 2.8)[id.takeLast(2).toInt() - 1],
        sampleSpaces = listOf(120, 450, 320, 80, 65, 200, 180, 150)[id.takeLast(2).toInt() - 1],
        sampleHourlyPrice = if (type == ParkingType.CAR_PARK) 2.5 else 1.0
    )
}
