package com.example.mobileapplication.ui.maps

import org.osmdroid.util.GeoPoint

/** Edges of an area to fit on screen, in degrees. */
data class MapBounds(val north: Double, val east: Double, val south: Double, val west: Double)

/** Pure framing maths, kept separate from MapView so it can be unit tested. */
object MapFraming {
    /** Smallest span (degrees) so that close-together points still leave some surrounding streets visible. */
    private const val MIN_SPAN = 0.004

    /**
     * Bounds that contain every point, padded to at least [MIN_SPAN]. Returns null for a single point
     * (or identical points), which callers show at a fixed street-level zoom instead.
     */
    fun boundsFor(points: List<GeoPoint>): MapBounds? {
        if (points.size < 2) return null
        var north = points.maxOf { it.latitude }
        var south = points.minOf { it.latitude }
        var east = points.maxOf { it.longitude }
        var west = points.minOf { it.longitude }
        if (north == south && east == west) return null
        if (north - south < MIN_SPAN) { val mid = (north + south) / 2; north = mid + MIN_SPAN / 2; south = mid - MIN_SPAN / 2 }
        if (east - west < MIN_SPAN) { val mid = (east + west) / 2; east = mid + MIN_SPAN / 2; west = mid - MIN_SPAN / 2 }
        return MapBounds(north, east, south, west)
    }

    /**
     * Extends the northern edge so the points are not hidden under overlays at the top of the map.
     * [fraction] is the share of the map height those overlays cover (0 = none).
     */
    fun withTopSpace(bounds: MapBounds, fraction: Double): MapBounds {
        if (fraction <= 0.0) return bounds
        val span = bounds.north - bounds.south
        return bounds.copy(north = bounds.north + span * fraction / (1 - fraction))
    }

    /**
     * Whether the user's position should be included when framing the car parks. A position far away
     * (for example an emulator's default location in California) would zoom the map out to the whole world.
     */
    fun isNearby(user: GeoPoint, points: List<GeoPoint>, maxDegrees: Double = 0.5): Boolean =
        points.any { kotlin.math.abs(it.latitude - user.latitude) <= maxDegrees && kotlin.math.abs(it.longitude - user.longitude) <= maxDegrees }

    /**
     * Converts an outline drawn in map-fraction units (x right, y down, 0..1) into coordinates around [centre].
     * Used to place the fictional sample zone around the sample address.
     */
    fun outlineAround(centre: GeoPoint, outline: List<Pair<Float, Float>>, anchor: Pair<Float, Float>, spanLon: Double, spanLat: Double): List<GeoPoint> =
        outline.map { (x, y) ->
            GeoPoint(centre.latitude - (y - anchor.second) * spanLat, centre.longitude + (x - anchor.first) * spanLon)
        }
}
