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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * A tappable tile with an icon and a label, used to pick one option from a set
 * (for example the room picker).
 *
 * Selected tiles get a 1.5dp teal border and the `surfaceTint` background.
 * Screen readers announce it as a radio button, so put the tiles inside a
 * `Modifier.selectableGroup()`.
 *
 * @param horizontal Lay the icon beside the label instead of above it. Useful for
 * longer labels that need the full width.
 */
@Composable
fun SelectableTile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.extendedColors.surfaceTint else MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (selected) {
            BorderStroke(Dimens.SelectedBorderWidth, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
    ) {
        if (horizontal) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.CardSpacing)
            ) {
                // Decorative: the label says what the tile is.
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(Dimens.CardSpacing))
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .heightIn(min = 112.dp)
                    .padding(Dimens.CardSpacing)
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(Dimens.CardSpacing))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectableTilePreview() {
    MobileApplicationTheme {
        Row(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)
        ) {
            SelectableTile("Kitchen", Icons.Filled.Kitchen, selected = true, onClick = {}, modifier = Modifier.weight(1f))
            SelectableTile("Bathroom", Icons.Filled.Bathtub, selected = false, onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}
