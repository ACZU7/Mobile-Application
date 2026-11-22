package com.example.mobileapplication.ui.maps

import com.example.mobileapplication.ui.parking.permit.SampleZoneGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.osmdroid.util.GeoPoint

class MapFramingTest {
    @Test fun singlePoint_hasNoBounds() {
        assertNull(MapFraming.boundsFor(listOf(GeoPoint(51.45, -2.59))))
        assertNull(MapFraming.boundsFor(listOf(GeoPoint(51.45, -2.59), GeoPoint(51.45, -2.59))))
    }

    @Test fun bounds_containEveryPoint() {
        val b = MapFraming.boundsFor(listOf(GeoPoint(51.41, -2.61), GeoPoint(51.51, -2.52), GeoPoint(51.45, -2.59)))!!
        assertEquals(51.51, b.north, 1e-9); assertEquals(51.41, b.south, 1e-9)
        assertEquals(-2.52, b.east, 1e-9); assertEquals(-2.61, b.west, 1e-9)
    }

    @Test fun closePoints_arePaddedToAMinimumSpan() {
        val b = MapFraming.boundsFor(listOf(GeoPoint(51.4500, -2.5900), GeoPoint(51.4501, -2.5901)))!!
        assertTrue(b.north - b.south >= 0.004 - 1e-9)
        assertTrue(b.east - b.west >= 0.004 - 1e-9)
    }

    @Test fun distantUser_isNotIncludedWhenFraming() {
        val bristol = listOf(GeoPoint(51.45, -2.59))
        assertTrue(MapFraming.isNearby(GeoPoint(51.47, -2.52), bristol))
        assertFalse(MapFraming.isNearby(GeoPoint(37.42, -122.08), bristol))
    }

    @Test fun sampleZone_surroundsTheSampleAddress() {
        val zone = SampleZoneGeometry.zoneOutline
        val address = SampleZoneGeometry.sampleAddress
        assertEquals(12, zone.size)
        assertTrue(zone.minOf { it.latitude } < address.latitude && zone.maxOf { it.latitude } > address.latitude)
        assertTrue(zone.minOf { it.longitude } < address.longitude && zone.maxOf { it.longitude } > address.longitude)
    }
}
