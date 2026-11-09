package com.example.mobileapplication.ui.parking.permit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * Mockup screen 4 map: an invented street illustration with the sample zone shaded coral and the
 * sample position marked. Drawn on the device; not geographic map data.
 */
@Composable
internal fun PermitZoneMap(zoneCode: String, modifier: Modifier = Modifier) {
    val colours = MaterialTheme.colorScheme
    val extended = MaterialTheme.extendedColors
    val description = stringResource(R.string.permit_map_description, zoneCode)
    // Polygon corners as fractions of the map size, traced from the approved mockup's zone outline.
    val zoneOutline = listOf(
        0.10f to 0.62f, 0.20f to 0.47f, 0.32f to 0.44f, 0.40f to 0.25f, 0.46f to 0.22f,
        0.70f to 0.38f, 0.82f to 0.70f, 0.86f to 0.86f, 0.70f to 0.97f, 0.56f to 0.92f,
        0.40f to 0.90f, 0.28f to 0.80f
    )
    BoxWithConstraints(modifier.background(extended.chipNeutral).clearAndSetSemantics { contentDescription = description }) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Soft blocks suggest buildings without copying a real map.
            for (row in 0..9) for (column in 0..6) {
                drawRoundRect(
                    color = colours.outlineVariant.copy(alpha = 0.5f),
                    topLeft = Offset(column * w / 6 + 4.dp.toPx(), row * h / 9 + 4.dp.toPx()),
                    size = Size(w / 6 - 8.dp.toPx(), h / 9 - 8.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
            // Parks.
            drawRoundRect(extended.success.copy(alpha = 0.16f), Offset(w * 0.74f, h * 0.10f), Size(w * 0.24f, h * 0.30f), CornerRadius(8.dp.toPx()))
            drawRoundRect(extended.success.copy(alpha = 0.16f), Offset(0f, h * 0.78f), Size(w * 0.18f, h * 0.20f), CornerRadius(8.dp.toPx()))
            // Main roads.
            val road = colours.surface
            drawLine(road, Offset(0f, h * 0.30f), Offset(w, h * 0.55f), 8.dp.toPx(), StrokeCap.Round)
            drawLine(road, Offset(w * 0.15f, 0f), Offset(w * 0.55f, h), 7.dp.toPx(), StrokeCap.Round)
            drawLine(road, Offset(w * 0.62f, 0f), Offset(w * 0.95f, h), 6.dp.toPx(), StrokeCap.Round)
            drawLine(road, Offset(0f, h * 0.85f), Offset(w, h * 0.72f), 6.dp.toPx(), StrokeCap.Round)
            // Sample zone.
            val zone = Path().apply {
                zoneOutline.forEachIndexed { index, (x, y) -> if (index == 0) moveTo(x * w, y * h) else lineTo(x * w, y * h) }
                close()
            }
            drawPath(zone, colours.secondary.copy(alpha = 0.22f))
            drawPath(zone, colours.secondary, style = Stroke(width = 2.dp.toPx()))
            // Sample position: halo plus dot.
            val position = Offset(w * 0.47f, h * 0.62f)
            val dot = extended.statusSubmitted.content
            drawCircle(dot.copy(alpha = 0.18f), radius = 16.dp.toPx(), center = position)
            drawCircle(colours.surface, radius = 8.dp.toPx(), center = position)
            drawCircle(dot, radius = 6.dp.toPx(), center = position)
        }
        Surface(
            shape = RoundedCornerShape(6.dp), color = colours.secondary, contentColor = colours.onSecondary,
            modifier = Modifier.offset(x = maxWidth * 0.26f, y = maxHeight * 0.40f)
        ) {
            Text(
                stringResource(R.string.permit_zone_label, zoneCode), style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        // Anchored to the end edge so the label stays on screen at large font sizes and in RTL.
        MapLabel(
            R.string.permit_map_label_park,
            Modifier.align(Alignment.TopEnd).padding(top = maxHeight * 0.18f, end = 8.dp).widthIn(max = maxWidth * 0.3f)
        )
        MapLabel(R.string.permit_map_label_area, Modifier.offset(x = maxWidth * 0.10f, y = maxHeight * 0.88f))
    }
}

@Composable
private fun MapLabel(textRes: Int, modifier: Modifier) {
    Text(
        stringResource(textRes), style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier
    )
}
