package com.example.mobileapplication.ui.housing

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.ui.shared.StatusChipType
import com.example.mobileapplication.ui.theme.Dimens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A card listing the details of a [request] (room, problem, appointment, and so on).
 *
 * It is shared by the confirmation and progress screens so both show the
 * details in the same way: a white card with a very light border, as in the spec.
 */
@Composable
fun RepairDetailsCard(
    request: RepairRequest,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)
        ) {
            DetailRow(R.string.housing_detail_room, stringResource(request.room.labelRes))
            DetailRow(R.string.housing_detail_issue, stringResource(request.issueType.labelRes))
            DetailRow(R.string.housing_detail_description, request.description)
            DetailRow(
                R.string.housing_detail_appointment,
                stringResource(request.appointmentPreference.labelRes)
            )
            DetailRow(R.string.housing_detail_created, formatDate(request.createdDate))
        }
    }
}

/** A white card with a very light border and no shadow, as the spec asks for. */
@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        content()
    }
}

/** One label/value pair, for example "Room" above "Kitchen". */
@Composable
private fun DetailRow(@StringRes labelRes: Int, value: String) {
    Column {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Which status-chip colours to use for this repair status. */
val RepairStatus.chipType: StatusChipType
    get() = when (this) {
        RepairStatus.SUBMITTED -> StatusChipType.SUBMITTED
        RepairStatus.APPOINTMENT_BOOKED, RepairStatus.IN_PROGRESS -> StatusChipType.IN_PROGRESS
        RepairStatus.COMPLETED -> StatusChipType.FIXED
    }

/** UK date style, for example "22 Oct 2026", whatever the phone's language. */
private val ukDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK)

/** Formats a date the UK way, for example "22 Oct 2026". */
fun formatDate(date: LocalDate): String = date.format(ukDateFormatter)
