package com.example.mobileapplication.ui.housing

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.RepairRequest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * A card listing the details of a [request] (room, problem, appointment, and so on).
 *
 * It is shared by the confirmation and progress screens so both show the
 * details in the same way.
 *
 * @param showDescription Whether to include the resident's full description.
 */
@Composable
fun RepairDetailsCard(
    request: RepairRequest,
    modifier: Modifier = Modifier,
    showDescription: Boolean = true
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailRow(R.string.housing_detail_room, stringResource(request.room.labelRes))
            DetailRow(R.string.housing_detail_issue, stringResource(request.issueType.labelRes))
            if (showDescription) {
                DetailRow(R.string.housing_detail_description, request.description)
            }
            DetailRow(
                R.string.housing_detail_appointment,
                stringResource(request.appointmentPreference.labelRes)
            )
            DetailRow(R.string.housing_detail_created, formatDate(request.createdDate))
        }
    }
}

/** One label/value pair, for example "Room" above "Kitchen". */
@Composable
private fun DetailRow(@StringRes labelRes: Int, value: String) {
    Column {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Formats a date in the phone's own style, for example "21 Oct 2026" in the UK. */
private fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
