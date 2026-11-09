package com.example.mobileapplication.ui.parking

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** A latitude/longitude pair, such as the user's current position. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/** Straight-line ("as the crow flies") distances. Good enough for sorting; not a route length. */
object DistanceCalculator {
    private const val EARTH_RADIUS_MILES = 3958.8

    /** Haversine distance in miles between two points. */
    fun miles(from: GeoPoint, to: GeoPoint): Double {
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_MILES * asin(sqrt(a))
    }
}
