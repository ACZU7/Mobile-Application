package com.example.mobileapplication.domain.cleanairzone

import com.example.mobileapplication.data.model.CazCheckResult
import com.example.mobileapplication.data.model.CazOutcome
import com.example.mobileapplication.data.model.EuroStandard
import com.example.mobileapplication.data.model.FuelType
import com.example.mobileapplication.data.model.SampleVehicle
import com.example.mobileapplication.data.model.VehicleType

/**
 * Works out a prototype Clean Air Zone result for a sample vehicle.
 *
 * It is plain Kotlin, kept separate from the ViewModel and the screens, so the
 * rules can be changed and unit tested without touching any UI code.
 *
 * Prototype rules (for demonstration only, not the official scheme):
 * - electric vehicles are always compliant;
 * - petrol vehicles are compliant from Euro 4;
 * - diesel vehicles are compliant from Euro 6;
 * - any other vehicle pays the sample daily charge for its type.
 */
object CazRulesEngine {

    /** Oldest petrol standard that meets the prototype rules. */
    val MIN_PETROL_STANDARD = EuroStandard.EURO_4

    /** Oldest diesel standard that meets the prototype rules. */
    val MIN_DIESEL_STANDARD = EuroStandard.EURO_6

    /** SAMPLE daily charge for cars, taxis and vans, in pence (£9.00). Not an official tariff. */
    const val SAMPLE_CHARGE_LIGHT_VEHICLE_PENCE = 900

    /** SAMPLE daily charge for buses and HGVs, in pence (£100.00). Not an official tariff. */
    const val SAMPLE_CHARGE_HEAVY_VEHICLE_PENCE = 10_000

    /**
     * Checks [vehicle] against the prototype rules.
     *
     * @param registration The registration as the user entered it; it is copied
     * into the result unchanged.
     * @param vehicle The matching sample vehicle, or null if the registration is
     * not in the sample data. A null vehicle gives a [CazOutcome.UNKNOWN] result.
     */
    fun check(registration: String, vehicle: SampleVehicle?): CazCheckResult {
        if (vehicle == null) {
            return CazCheckResult(
                registration = registration,
                vehicleType = null,
                fuelType = null,
                euroStandard = null,
                outcome = CazOutcome.UNKNOWN
            )
        }
        val outcome = outcomeFor(vehicle.fuelType, vehicle.euroStandard)
        return CazCheckResult(
            registration = registration,
            vehicleType = vehicle.vehicleType,
            fuelType = vehicle.fuelType,
            euroStandard = vehicle.euroStandard,
            outcome = outcome,
            dailyChargePence = if (outcome == CazOutcome.CHARGE_APPLIES) {
                sampleDailyChargePence(vehicle.vehicleType)
            } else {
                null
            }
        )
    }

    /**
     * Applies the fuel and Euro standard rules.
     *
     * A petrol or diesel vehicle with no known Euro standard gives
     * [CazOutcome.UNKNOWN], because the rules cannot be applied.
     */
    private fun outcomeFor(fuelType: FuelType, euroStandard: EuroStandard?): CazOutcome {
        val minimum = when (fuelType) {
            FuelType.ELECTRIC -> return CazOutcome.NO_CHARGE
            FuelType.PETROL -> MIN_PETROL_STANDARD
            FuelType.DIESEL -> MIN_DIESEL_STANDARD
        }
        return when {
            euroStandard == null -> CazOutcome.UNKNOWN
            euroStandard.level >= minimum.level -> CazOutcome.NO_CHARGE
            else -> CazOutcome.CHARGE_APPLIES
        }
    }

    /** Returns the SAMPLE daily charge for a vehicle of this type, in pence. */
    fun sampleDailyChargePence(vehicleType: VehicleType): Int = when (vehicleType) {
        VehicleType.CAR, VehicleType.TAXI, VehicleType.VAN -> SAMPLE_CHARGE_LIGHT_VEHICLE_PENCE
        VehicleType.BUS, VehicleType.HGV -> SAMPLE_CHARGE_HEAVY_VEHICLE_PENCE
    }
}
