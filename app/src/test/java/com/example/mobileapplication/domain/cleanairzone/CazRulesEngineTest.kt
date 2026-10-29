package com.example.mobileapplication.domain.cleanairzone

import com.example.mobileapplication.data.model.CazOutcome
import com.example.mobileapplication.data.model.EuroStandard
import com.example.mobileapplication.data.model.FuelType
import com.example.mobileapplication.data.model.SampleVehicle
import com.example.mobileapplication.data.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for the prototype Clean Air Zone rules in [CazRulesEngine]. */
class CazRulesEngineTest {

    private fun vehicle(
        fuelType: FuelType,
        euroStandard: EuroStandard?,
        vehicleType: VehicleType = VehicleType.CAR
    ) = SampleVehicle("IZ00 TST", vehicleType, fuelType, euroStandard)

    private fun outcomeOf(fuelType: FuelType, euroStandard: EuroStandard?): CazOutcome =
        CazRulesEngine.check("IZ00 TST", vehicle(fuelType, euroStandard)).outcome

    // Electric

    @Test
    fun electric_withNoEuroStandard_hasNoCharge() {
        val result = CazRulesEngine.check("IZ00 TST", vehicle(FuelType.ELECTRIC, null))

        assertEquals(CazOutcome.NO_CHARGE, result.outcome)
        assertNull(result.dailyChargePence)
    }

    @Test
    fun electric_isCompliantWhateverTheEuroStandard() {
        EuroStandard.entries.forEach {
            assertEquals(CazOutcome.NO_CHARGE, outcomeOf(FuelType.ELECTRIC, it))
        }
    }

    @Test
    fun electricHgv_hasNoCharge() {
        val result = CazRulesEngine.check(
            "IZ00 TST",
            vehicle(FuelType.ELECTRIC, null, VehicleType.HGV)
        )
        assertEquals(CazOutcome.NO_CHARGE, result.outcome)
    }

    // Petrol: compliant from Euro 4

    @Test
    fun petrolEuro3_isCharged() {
        assertEquals(CazOutcome.CHARGE_APPLIES, outcomeOf(FuelType.PETROL, EuroStandard.EURO_3))
    }

    @Test
    fun petrolEuro4_hasNoCharge() {
        assertEquals(CazOutcome.NO_CHARGE, outcomeOf(FuelType.PETROL, EuroStandard.EURO_4))
    }

    @Test
    fun petrol_everyStandardBelowEuro4IsCharged_andEuro4UpIsNot() {
        EuroStandard.entries.forEach {
            val expected = if (it.level >= 4) CazOutcome.NO_CHARGE else CazOutcome.CHARGE_APPLIES
            assertEquals("Petrol $it", expected, outcomeOf(FuelType.PETROL, it))
        }
    }

    // Diesel: compliant from Euro 6

    @Test
    fun dieselEuro5_isCharged() {
        assertEquals(CazOutcome.CHARGE_APPLIES, outcomeOf(FuelType.DIESEL, EuroStandard.EURO_5))
    }

    @Test
    fun dieselEuro6_hasNoCharge() {
        assertEquals(CazOutcome.NO_CHARGE, outcomeOf(FuelType.DIESEL, EuroStandard.EURO_6))
    }

    @Test
    fun diesel_everyStandardBelowEuro6IsCharged_andEuro6IsNot() {
        EuroStandard.entries.forEach {
            val expected = if (it.level >= 6) CazOutcome.NO_CHARGE else CazOutcome.CHARGE_APPLIES
            assertEquals("Diesel $it", expected, outcomeOf(FuelType.DIESEL, it))
        }
    }

    // Missing information

    @Test
    fun petrolWithNoEuroStandard_isUnknown() {
        assertEquals(CazOutcome.UNKNOWN, outcomeOf(FuelType.PETROL, null))
    }

    @Test
    fun dieselWithNoEuroStandard_isUnknown() {
        assertEquals(CazOutcome.UNKNOWN, outcomeOf(FuelType.DIESEL, null))
    }

    @Test
    fun registrationNotInSampleData_isUnknownWithNoVehicleDetails() {
        val result = CazRulesEngine.check("AB12 CDE", vehicle = null)

        assertEquals(CazOutcome.UNKNOWN, result.outcome)
        assertEquals("AB12 CDE", result.registration)
        assertNull(result.vehicleType)
        assertNull(result.fuelType)
        assertNull(result.euroStandard)
        assertNull(result.dailyChargePence)
    }

    // Sample charges

    @Test
    fun chargedLightVehicles_payTheLightSampleCharge() {
        listOf(VehicleType.CAR, VehicleType.TAXI, VehicleType.VAN).forEach { type ->
            val result = CazRulesEngine.check(
                "IZ00 TST",
                vehicle(FuelType.DIESEL, EuroStandard.EURO_5, type)
            )
            assertEquals(
                "$type",
                CazRulesEngine.SAMPLE_CHARGE_LIGHT_VEHICLE_PENCE,
                result.dailyChargePence
            )
        }
    }

    @Test
    fun chargedHeavyVehicles_payTheHeavySampleCharge() {
        listOf(VehicleType.BUS, VehicleType.HGV).forEach { type ->
            val result = CazRulesEngine.check(
                "IZ00 TST",
                vehicle(FuelType.DIESEL, EuroStandard.EURO_5, type)
            )
            assertEquals(
                "$type",
                CazRulesEngine.SAMPLE_CHARGE_HEAVY_VEHICLE_PENCE,
                result.dailyChargePence
            )
        }
    }

    @Test
    fun compliantVehicle_hasNoDailyCharge() {
        val result = CazRulesEngine.check(
            "IZ00 TST",
            vehicle(FuelType.DIESEL, EuroStandard.EURO_6, VehicleType.HGV)
        )
        assertNull(result.dailyChargePence)
    }

    // Result details

    @Test
    fun result_copiesTheVehicleDetailsAndRegistrationAsEntered() {
        val sample = vehicle(FuelType.PETROL, EuroStandard.EURO_4, VehicleType.VAN)
        val result = CazRulesEngine.check("iz00tst", sample)

        assertEquals("iz00tst", result.registration)
        assertEquals(VehicleType.VAN, result.vehicleType)
        assertEquals(FuelType.PETROL, result.fuelType)
        assertEquals(EuroStandard.EURO_4, result.euroStandard)
    }
}
