package com.example.mobileapplication.ui.parking

import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionsAndDistanceTest {

    @Test
    fun googleMapsDirections_usesDestinationCoordinates() {
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=51.454000,-2.593000",
            DirectionsUris.googleMapsDirections(51.454, -2.593)
        )
    }

    @Test
    fun geoUri_includesEncodedLabel() {
        assertEquals(
            "geo:51.410000,-2.610000?q=51.410000,-2.610000(Sample%20South)",
            DirectionsUris.geo(51.41, -2.61, "Sample%20South")
        )
    }

    @Test
    fun samePoint_isZeroMiles() {
        val point = GeoPoint(51.45, -2.59)
        assertEquals(0.0, DistanceCalculator.miles(point, point), 0.0)
    }

    @Test
    fun knownDistance_isWithinHalfAPercent() {
        // Bristol Temple Meads to London Paddington is roughly 104 miles in a straight line.
        val bristol = GeoPoint(51.4491, -2.5813)
        val london = GeoPoint(51.5154, -0.1755)
        assertEquals(103.9, DistanceCalculator.miles(bristol, london), 0.5)
    }
}
