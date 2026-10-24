package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.ParkingType
import org.junit.Assert.*
import org.junit.Test

class ParkingRepositoryTest {
    private val repository = ParkingRepository()

    @Test
    fun sampleLocations_haveUniqueIdsAndBothParkingTypes() {
        val locations = repository.carParks
        assertEquals(8, locations.size)
        assertEquals(locations.size, locations.map { it.id }.distinct().size)
        assertEquals(ParkingType.entries.toSet(), locations.map { it.type }.toSet())
        assertTrue(locations.all { it.latitude in 51.3..51.6 && it.longitude in -2.8..-2.4 })
    }

    @Test
    fun getCarPark_returnsMatchingLocation() {
        val expected = repository.carParks[2]
        assertEquals(expected, repository.getCarPark(expected.id))
    }

    @Test
    fun getCarPark_unknownIdReturnsNull() {
        assertNull(repository.getCarPark("missing"))
    }
}
