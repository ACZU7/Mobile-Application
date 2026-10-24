package com.example.mobileapplication.ui.parking

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.CarPark
import com.example.mobileapplication.ui.theme.extendedColors

/** An invented street illustration, drawn entirely on-device. Not geographic map data. */
@Composable
internal fun StaticParkingMap(
    carParks: List<CarPark>,
    highlightedId: String?,
    onPinSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colours = MaterialTheme.colorScheme
    val extended = MaterialTheme.extendedColors
    val description = stringResource(R.string.parking_map_description)
    // These are fixed illustration positions, not GPS coordinates or geographic projections.
    val positions = listOf(
        0.70f to 0.50f, 0.55f to 0.32f, 0.23f to 0.38f, 0.38f to 0.62f,
        0.11f to 0.78f, 0.83f to 0.76f, 0.17f to 0.61f, 0.86f to 0.34f
    )
    BoxWithConstraints(modifier.background(extended.chipNeutral)) {
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = description }) {
            val w = size.width
            val h = size.height
            // Soft blocks suggest buildings and open space without copying a real map.
            for (row in 0..8) {
                for (column in 0..7) {
                    drawRoundRect(
                        color = colours.outlineVariant.copy(alpha = 0.55f),
                        topLeft = Offset(column * w / 7 + 5.dp.toPx(), row * h / 8 + 5.dp.toPx()),
                        size = Size(w / 9, h / 12), cornerRadius = CornerRadius(3.dp.toPx())
                    )
                }
            }
            listOf(0.02f to 0.43f, 0.76f to 0.20f, 0.36f to 0.84f, 0.82f to 0.58f).forEach { (x, y) ->
                drawRoundRect(
                    extended.success.copy(alpha = 0.18f), Offset(w * x, h * y),
                    Size(w * 0.14f, h * 0.10f), CornerRadius(6.dp.toPx())
                )
            }
            // Curved harbour/river, using an existing shared status colour token.
            val river = Path().apply {
                moveTo(-w * 0.1f, h * 0.86f)
                cubicTo(w * 0.48f, h * 1.14f, w * 0.13f, h * 0.58f, w * 0.48f, h * 0.59f)
                cubicTo(w * 0.82f, h * 0.62f, w * 0.81f, h * 0.39f, w * 1.1f, h * 0.44f)
            }
            drawPath(river, extended.statusSubmitted.container, style = Stroke(width = 21.dp.toPx(), cap = StrokeCap.Round))
            // A small lattice of curved and diagonal streets.
            for (street in -5..10) {
                val road = Path().apply {
                    moveTo(street * w / 7, -h * 0.1f)
                    cubicTo(street * w / 7 + w * 0.25f, h * 0.3f, street * w / 7 - w * 0.3f, h * 0.6f, street * w / 7 + w * 0.18f, h * 1.1f)
                }
                drawPath(road, colours.outlineVariant, style = Stroke(width = 5.dp.toPx()))
                drawPath(road, colours.surface, style = Stroke(width = 3.dp.toPx()))
            }
            for (street in -3..10) {
                val start = Offset(-w * 0.1f, street * h / 7)
                val end = Offset(w * 1.1f, street * h / 7 - h * 0.32f)
                drawLine(colours.outlineVariant, start, end, strokeWidth = 5.dp.toPx())
                drawLine(colours.surface, start, end, strokeWidth = 3.dp.toPx())
            }
            // A wider local road across the lower illustration.
            drawLine(colours.surface, Offset(w * 0.04f, h), Offset(w * 0.92f, h * 0.15f), strokeWidth = 7.dp.toPx())
        }
        Text(
            stringResource(R.string.parking_bristol_label), style = MaterialTheme.typography.titleSmall,
            color = colours.onSurfaceVariant.copy(alpha = 0.7f), letterSpacing = 3.sp,
            modifier = Modifier.offset(x = maxWidth * 0.34f, y = maxHeight * 0.73f)
        )
        carParks.forEach { carPark ->
            val position = positions[(carPark.id.takeLast(2).toInt() - 1) % positions.size]
            val selected = carPark.id == highlightedId
            val pinDescription = stringResource(R.string.parking_pin_description, stringResource(carPark.nameRes))
            Box(Modifier.offset(x = maxWidth * position.first - 24.dp, y = maxHeight * position.second - 24.dp)) {
                IconButton(
                    onClick = { onPinSelected(carPark.id) },
                    modifier = Modifier.size(48.dp).semantics { contentDescription = pinDescription; this.selected = selected }
                ) {
                    Box(Modifier.size(width = 30.dp, height = 40.dp), contentAlignment = Alignment.TopCenter) {
                        Canvas(Modifier.fillMaxSize()) {
                            val pin = Path().apply {
                                moveTo(size.width / 2, size.height)
                                cubicTo(-size.width * 0.1f, size.height * 0.42f, 0f, 0f, size.width / 2, 0f)
                                cubicTo(size.width, 0f, size.width * 1.1f, size.height * 0.42f, size.width / 2, size.height)
                                close()
                            }
                            drawPath(pin, colours.surface, style = Stroke(width = 3.dp.toPx()))
                            drawPath(pin, if (selected) colours.secondary else colours.primary)
                        }
                        Text(
                            stringResource(R.string.parking_pin_letter), style = MaterialTheme.typography.titleSmall,
                            color = if (selected) colours.onSecondary else colours.onPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (selected) {
                    Surface(
                        shape = RoundedCornerShape(4.dp), color = colours.primary, contentColor = colours.onPrimary,
                        modifier = Modifier.offset(x = (-14).dp, y = 46.dp)
                    ) {
                        Text(
                            stringResource(R.string.parking_map_spaces, carPark.sampleSpaces),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
