package com.example.mobileapplication.ui.housing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * A tappable row in a card, following the spec's ListRowCard: an icon on the
 * left, a title and a secondary line, optional extra content, and a chevron on
 * the right.
 *
 * Used by the Housing home options, the "My repairs" list and the rent account
 * links, so they all look the same.
 *
 * @param icon The icon on the left. It is decorative, because the title says what the row is.
 * @param title The main text, for example "Report a repair".
 * @param supportingText The grey second line, or null for none.
 * @param onClick Called when the row is tapped.
 * @param trailing Optional content shown before the chevron, for example a status chip.
 * @param extra Optional content shown under the supporting text, for example a "Sample" label.
 */
@Composable
fun HousingListRow(
    icon: ImageVector,
    title: String,
    supportingText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
    extra: @Composable () -> Unit = {}
) {
    AppCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .heightIn(min = 64.dp)
                .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.CardSpacing)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.extendedColors.surfaceTint, CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                extra()
            }
            trailing()
            // AutoMirrored so the chevron points the right way in Arabic (RTL).
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Preview of a single row. */
@Preview(showBackground = true)
@Composable
private fun HousingListRowPreview() {
    MobileApplicationTheme {
        HousingListRow(
            icon = Icons.Filled.Build,
            title = "Report a repair",
            supportingText = "Tell us about something that needs fixing",
            onClick = {},
            modifier = Modifier.padding(Dimens.ScreenPadding)
        )
    }
}
