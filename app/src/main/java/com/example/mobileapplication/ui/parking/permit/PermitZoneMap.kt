package com.example.mobileapplication.ui.parking.permit

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.maps.MapFraming
import com.example.mobileapplication.ui.maps.MapMarkers
import com.example.mobileapplication.ui.maps.OsmMap
import com.example.mobileapplication.ui.maps.addMarker
import com.example.mobileapplication.ui.maps.frame
import com.example.mobileapplication.ui.theme.extendedColors
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

/** Where the fictional sample zone is drawn. The outline is invented and is not a real permit-zone boundary. */
object SampleZoneGeometry {
    /** The sample address shown in the search bar ("College Road, Bristol"). */
    val sampleAddress = GeoPoint(51.4612, -2.6195)

    // The outline traced from mockup 4, in map-fraction units, with the sample address at (0.47, 0.62).
    private val outline = listOf(
        0.10f to 0.62f, 0.20f to 0.47f, 0.32f to 0.44f, 0.40f to 0.25f, 0.46f to 0.22f,
        0.70f to 0.38f, 0.82f to 0.70f, 0.86f to 0.86f, 0.70f to 0.97f, 0.56f to 0.92f,
        0.40f to 0.90f, 0.28f to 0.80f
    )
    private val anchor = 0.47f to 0.62f
    private const val SPAN_LON = 0.016
    private const val SPAN_LAT = 0.009

    val zoneOutline: List<GeoPoint> = MapFraming.outlineAround(sampleAddress, outline, anchor, SPAN_LON, SPAN_LAT)
    val labelPosition: GeoPoint = MapFraming.outlineAround(sampleAddress, listOf(0.33f to 0.42f), anchor, SPAN_LON, SPAN_LAT).single()
}

/**
 * Mockup screen 4 map on real OpenStreetMap tiles: the fictional sample zone shaded coral around the sample
 * address, with the sample position marked. The screen shows a prominent "sample zone" banner over it.
 */
@Composable
internal fun PermitZoneMap(zoneCode: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colours = MaterialTheme.colorScheme
    val zoneColour = colours.secondary
    val badgeText = colours.onSecondary.toArgb()
    val ring = colours.surface.toArgb()
    val dot = MaterialTheme.extendedColors.statusSubmitted.content.toArgb()
    val label = stringResource(R.string.permit_zone_label, zoneCode)
    val framed = remember { booleanArrayOf(false) }

    OsmMap(stringResource(R.string.permit_map_description, zoneCode), modifier, attributionBottomPadding = 24.dp) { map ->
        map.overlays.removeAll { it is Marker || it is Polygon }
        map.overlays.add(Polygon(map).apply {
            points = SampleZoneGeometry.zoneOutline + SampleZoneGeometry.zoneOutline.first()
            fillPaint.color = zoneColour.copy(alpha = 0.22f).toArgb()
            outlinePaint.color = zoneColour.toArgb()
            outlinePaint.strokeWidth = 2 * context.resources.displayMetrics.density
            setOnClickListener { _, _, _ -> false }
            infoWindow = null
        })
        map.addMarker(SampleZoneGeometry.labelPosition, MapMarkers.badge(context, label, zoneColour.toArgb(), badgeText))
        map.addMarker(SampleZoneGeometry.sampleAddress, MapMarkers.locationDot(context, dot, ring))
        if (!framed[0]) {
            framed[0] = true
            map.frame(SampleZoneGeometry.zoneOutline, topOverlayFraction = 0.3)
        }
        map.invalidate()
    }
}
