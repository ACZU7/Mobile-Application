package com.example.mobileapplication.ui.parking

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.CarPark
import com.example.mobileapplication.ui.maps.MapFraming
import com.example.mobileapplication.ui.maps.MapMarkers
import com.example.mobileapplication.ui.maps.OsmMap
import com.example.mobileapplication.ui.maps.addMarker
import com.example.mobileapplication.ui.maps.frame
import com.example.mobileapplication.ui.theme.extendedColors
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint as OsmPoint

/**
 * Mockup screen 3 map on real OpenStreetMap tiles. Pins mark the sample car parks' coordinates; tapping a pin
 * highlights it (coral, with its sample spaces). After "Find my location" the user's position is shown too.
 */
@Composable
internal fun ParkingMap(
    carParks: List<CarPark>,
    highlightedId: String?,
    userLocation: GeoPoint?,
    onPinSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    attributionBottomPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val colours = MaterialTheme.colorScheme
    val pinColour = colours.primary.toArgb()
    val onPin = colours.onPrimary.toArgb()
    val selectedColour = colours.secondary.toArgb()
    val onSelected = colours.onSecondary.toArgb()
    val outline = colours.surface.toArgb()
    val dotColour = MaterialTheme.extendedColors.statusSubmitted.content.toArgb()
    val letter = stringResource(R.string.parking_pin_letter)
    val spacesLabels = carParks.associate { it.id to stringResource(R.string.parking_map_spaces, it.sampleSpaces) }
    // Re-frame only when the set of results or the user's position changes, not on every recomposition,
    // so the user's own panning and zooming is kept.
    val framing = remember { FramingKey() }

    OsmMap(stringResource(R.string.parking_map_description), modifier, attributionBottomPadding = attributionBottomPadding) { map ->
        map.overlays.removeAll { it is Marker }
        val points = carParks.map { OsmPoint(it.latitude, it.longitude) }
        // Markers draw in order, so the selected pin goes last and is never hidden by a neighbour.
        carParks.sortedBy { it.id == highlightedId }.forEach { carPark ->
            val selected = carPark.id == highlightedId
            val icon = if (selected) {
                MapMarkers.pin(context, selectedColour, outline, letter, onSelected, spacesLabels[carPark.id], pinColour, onPin)
            } else {
                MapMarkers.pin(context, pinColour, outline, letter, onPin)
            }
            map.addMarker(OsmPoint(carPark.latitude, carPark.longitude), icon, MapMarkers.pinAnchorY(icon, context)) {
                onPinSelected(carPark.id)
            }
        }
        val user = userLocation?.let { OsmPoint(it.latitude, it.longitude) }
        if (user != null) map.addMarker(user, MapMarkers.locationDot(context, dotColour, outline))
        val key = carParks.map { it.id } to userLocation
        if (framing.last != key) {
            framing.last = key
            val includeUser = user != null && MapFraming.isNearby(user, points)
            // The search bar and filter chips cover roughly the top third of the map.
            map.frame(if (includeUser) points + user!! else points, topOverlayFraction = 0.3)
        }
        map.invalidate()
    }
}

/** Holds the last framed state between recompositions. */
private class FramingKey { var last: Any? = null }
