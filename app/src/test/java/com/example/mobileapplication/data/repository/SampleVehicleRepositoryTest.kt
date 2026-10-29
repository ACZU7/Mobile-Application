package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.CazOutcome
import com.example.mobileapplication.data.model.EuroStandard
import com.example.mobileapplication.data.model.FuelType
import com.example.mobileapplication.domain.cleanairzone.CazRulesEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [SampleVehicleRepository] and its demo data. */
class SampleVehicleRepositoryTest {

    private val repository = SampleVehicleRepository()

    @Test
    fun findByRegistration_ignoresCaseAndSpaces() {
        val expected = repository.findByRegistration("IB15 PTL")

        assertNotNull(expected)
        assertEquals(expected, repository.findByRegistration("ib15ptl"))
        assertEquals(expected, repository.findByRegistration("  I B 1 5 P T L "))
    }

    @Test
    fun unknownRegistration_isNotFound() {
        assertNull(repository.findByRegistration("AB12 CDE"))
        assertNull(repository.findByRegistration(""))
    }

    @Test
    fun everyDemoRegistration_startsWithI_soItCannotBeARealVehicle() {
        repository.registrations.forEach {
            assertTrue("$it must start with I", it.startsWith("I"))
        }
    }

    @Test
    fun demoRegistrations_areUnique() {
        val keys = repository.registrations.map(SampleVehicleRepository::normalise)
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun sampleData_coversEveryOutcomeAndBoundaryCase() {
        val vehicles = repository.registrations.mapNotNull(repository::findByRegistration)
        val outcomes = vehicles.map { CazRulesEngine.check(it.registration, it).outcome }.toSet()

        assertTrue(CazOutcome.NO_CHARGE in outcomes)
        assertTrue(CazOutcome.CHARGE_APPLIES in outcomes)
        assertTrue(vehicles.any { it.fuelType == FuelType.ELECTRIC })
        assertTrue(vehicles.any { it.fuelType == FuelType.PETROL && it.euroStandard == EuroStandard.EURO_3 })
        assertTrue(vehicles.any { it.fuelType == FuelType.PETROL && it.euroStandard == EuroStandard.EURO_4 })
        assertTrue(vehicles.any { it.fuelType == FuelType.DIESEL && it.euroStandard == EuroStandard.EURO_5 })
        assertTrue(vehicles.any { it.fuelType == FuelType.DIESEL && it.euroStandard == EuroStandard.EURO_6 })
    }
}
