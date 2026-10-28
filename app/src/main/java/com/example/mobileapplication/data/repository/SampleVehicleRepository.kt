package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.EuroStandard
import com.example.mobileapplication.data.model.FuelType
import com.example.mobileapplication.data.model.SampleVehicle
import com.example.mobileapplication.data.model.VehicleType

/**
 * Looks up made-up demo registrations in memory.
 *
 * The app never tries to identify a real vehicle. Every demo registration starts
 * with the letter "I", which is never used at the start of a real UK registration,
 * so none of them can belong to a real vehicle. Any other registration is simply
 * not found.
 *
 * @param vehicles The vehicles to search. Tests can pass their own list.
 */
class SampleVehicleRepository(
    private val vehicles: List<SampleVehicle> = sampleVehicles()
) {

    /** The demo registrations, so a screen can suggest some to try. */
    val registrations: List<String> get() = vehicles.map { it.registration }

    /**
     * Returns the sample vehicle with this [registration], or null if there isn't one.
     *
     * Capital letters and spaces are ignored, so "ib15ptl" finds "IB15 PTL".
     */
    fun findByRegistration(registration: String): SampleVehicle? {
        val key = normalise(registration)
        return vehicles.find { normalise(it.registration) == key }
    }

    /** Holds the shared app-wide instance and the sample data. */
    companion object {

        /** One repository shared by the whole app. */
        val shared: SampleVehicleRepository by lazy { SampleVehicleRepository() }

        /** Removes all spaces and makes the letters upper case, so registrations can be compared. */
        fun normalise(registration: String): String =
            registration.filterNot { it.isWhitespace() }.uppercase()

        /**
         * Fictional vehicles covering each prototype rule, including the
         * boundary cases (petrol Euro 3 and 4, diesel Euro 5 and 6).
         */
        private fun sampleVehicles(): List<SampleVehicle> = listOf(
            SampleVehicle("IA26 EVD", VehicleType.CAR, FuelType.ELECTRIC, euroStandard = null),
            SampleVehicle("IB15 PTL", VehicleType.CAR, FuelType.PETROL, EuroStandard.EURO_4),
            SampleVehicle("IC05 PTL", VehicleType.CAR, FuelType.PETROL, EuroStandard.EURO_3),
            SampleVehicle("ID18 DSL", VehicleType.VAN, FuelType.DIESEL, EuroStandard.EURO_6),
            SampleVehicle("IE13 DSL", VehicleType.TAXI, FuelType.DIESEL, EuroStandard.EURO_5),
            SampleVehicle("IF12 HGV", VehicleType.HGV, FuelType.DIESEL, EuroStandard.EURO_5)
        )
    }
}
