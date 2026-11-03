package com.example.mobileapplication.data.model

/**
 * A made-up vehicle used to demonstrate the Clean Air Zone checker.
 *
 * It does not describe any real vehicle; see
 * [com.example.mobileapplication.data.repository.SampleVehicleRepository].
 *
 * @property registration The demo registration, for example "IB15 PTL".
 * @property make The manufacturer, for example "Volkswagen".
 * @property model The model name, for example "Golf".
 * @property vehicleType The kind of vehicle.
 * @property fuelType The vehicle's fuel.
 * @property euroStandard The emissions standard, or null for electric vehicles
 * (which do not have one).
 */
data class SampleVehicle(
    val registration: String,
    val make: String,
    val model: String,
    val vehicleType: VehicleType,
    val fuelType: FuelType,
    val euroStandard: EuroStandard?
)
