package com.example.mobileapplication.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.ExtendedColors
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.StatusColors
import com.example.mobileapplication.ui.theme.extendedColors

/** The colour families a [StatusChip] can use, as defined in the UI spec. */
enum class StatusChipType(val colors: (ExtendedColors) -> StatusColors) {
    SUBMITTED({ it.statusSubmitted }),
    IN_PROGRESS({ it.statusInProgress }),
    FIXED({ it.statusFixed }),
    NEW({ it.statusNew })
}

/**
 * A small, fully rounded chip showing a status such as "Submitted" or "In progress".
 *
 * The [text] always states the status, so the meaning never depends on colour alone.
 */
@Composable
fun StatusChip(text: String, type: StatusChipType, modifier: Modifier = Modifier) {
    val colors = type.colors(MaterialTheme.extendedColors)
    Surface(
        shape = CircleShape,
        color = colors.container,
        contentColor = colors.content,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    MobileApplicationTheme {
        Row(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)
        ) {
            StatusChip("Submitted", StatusChipType.SUBMITTED)
            StatusChip("In progress", StatusChipType.IN_PROGRESS)
            StatusChip("Fixed", StatusChipType.FIXED)
            StatusChip("New", StatusChipType.NEW)
        }
    }
}
