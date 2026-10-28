package com.example.mobileapplication.data.model

/**
 * A made-up vehicle used to demonstrate the Clean Air Zone checker.
 *
 * It does not describe any real vehicle; see
 * [com.example.mobileapplication.data.repository.SampleVehicleRepository].
 *
 * @property registration The demo registration, for example "IB15 PTL".
 * @property vehicleType The kind of vehicle.
 * @property fuelType The vehicle's fuel.
 * @property euroStandard The emissions standard, or null for electric vehicles
 * (which do not have one).
 */
data class SampleVehicle(
    val registration: String,
    val vehicleType: VehicleType,
    val fuelType: FuelType,
    val euroStandard: EuroStandard?
)
