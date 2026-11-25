package com.example.mobileapplication.ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

/**
 * A service tile for the Home grid: a card with a centred icon and a label of up
 * to two lines underneath (the spec's ServiceTile).
 *
 * The label grows to more lines with very large fonts rather than being cut off.
 *
 * @param label What the tile opens, for example "Find parking".
 * @param icon Decorative icon; the label says what the tile is.
 * @param iconTint Teal by default; the mockup uses coral for "Report an issue" and "Rent account".
 */
@Composable
fun ServiceTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .heightIn(min = 104.dp)
                .padding(horizontal = Dimens.GroupSpacing, vertical = Dimens.CardSpacing)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(Dimens.CardSpacing))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ServiceTilePreview() {
    MobileApplicationTheme {
        Row(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)
        ) {
            ServiceTile("Find parking", Icons.Filled.DirectionsCar, onClick = {}, modifier = Modifier.weight(1f))
            ServiceTile(
                "Report an issue",
                Icons.Filled.Warning,
                onClick = {},
                iconTint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
