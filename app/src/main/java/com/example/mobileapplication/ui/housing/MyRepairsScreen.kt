package com.example.mobileapplication.ui.housing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.StatusChip
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors
import java.time.LocalDate

/**
 * Lists every saved repair request, newest first, with its reference, room,
 * date and status chip. Tapping a row opens its progress screen.
 *
 * @param requests The saved requests, or null while they are still loading.
 * @param onRequestClick Called with the request the user tapped.
 * @param onReportRepair Called from the empty state's "Report a repair" button.
 * @param onBack Called when the back arrow is tapped.
 */
@Composable
fun MyRepairsScreen(
    requests: List<RepairRequest>?,
    onRequestClick: (RepairRequest) -> Unit,
    onReportRepair: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.housing_my_repairs_title), onBack = onBack) }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                requests == null -> LoadingState()
                requests.isEmpty() -> EmptyState(onReportRepair)
                else -> RepairList(requests, onRequestClick)
            }
        }
    }
}

/** The scrolling list of request rows, with the demonstration notice at the top. */
@Composable
private fun RepairList(requests: List<RepairRequest>, onRequestClick: (RepairRequest) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        modifier = Modifier
            .widthIn(max = Dimens.MaxContentWidth)
            .fillMaxWidth()
    ) {
        item {
            Text(
                text = stringResource(R.string.housing_demo_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(requests, key = { it.reference }) { request ->
            RepairRow(request = request, onClick = { onRequestClick(request) })
        }
    }
}

/** One request: problem type, reference and room, date, a "Sample" label if needed, and its status. */
@Composable
private fun RepairRow(request: RepairRequest, onClick: () -> Unit) {
    HousingListRow(
        icon = request.issueType.icon,
        title = stringResource(request.issueType.labelRes),
        supportingText = stringResource(
            R.string.housing_my_repairs_row_subtitle,
            request.reference,
            stringResource(request.room.labelRes)
        ),
        onClick = onClick,
        extra = {
            Text(
                text = stringResource(R.string.housing_my_repairs_row_date, formatDate(request.createdDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (request.isSample) {
                Text(
                    text = stringResource(R.string.housing_my_repairs_sample_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        },
        trailing = {
            StatusChip(text = stringResource(request.status.labelRes), type = request.status.chipType)
        }
    )
}

/** Shown while the saved requests are loading from the database. */
@Composable
private fun LoadingState() {
    CircularProgressIndicator(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 48.dp)
    )
}

/** Shown when there are no saved requests, with a button to report the first one. */
@Composable
private fun EmptyState(onReportRepair: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
        modifier = Modifier
            .widthIn(max = Dimens.MaxContentWidth)
            .fillMaxWidth()
            .padding(Dimens.ScreenPadding)
            .padding(top = 32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.extendedColors.surfaceTint, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Build,
                contentDescription = null, // decorative: the heading says what this is
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = stringResource(R.string.housing_my_repairs_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.housing_my_repairs_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        PrimaryButton(
            text = stringResource(R.string.housing_home_report_repair),
            onClick = onReportRepair,
            modifier = Modifier.padding(top = Dimens.GroupSpacing)
        )
    }
}

/** Requests used by the previews: one the user submitted and one sample. */
private val previewRequests = listOf(
    RepairRequest(
        reference = "HR-2026-0004",
        room = Room.KITCHEN,
        issueType = IssueType.LEAK,
        description = "Water is dripping under the sink.",
        appointmentPreference = AppointmentPreference.MORNING,
        createdDate = LocalDate.of(2026, 11, 11),
        status = RepairStatus.SUBMITTED
    ),
    RepairRequest(
        reference = "HR-2026-0003",
        room = Room.KITCHEN,
        issueType = IssueType.HEATING,
        description = "Boiler shows an error code.",
        appointmentPreference = AppointmentPreference.AFTERNOON,
        createdDate = LocalDate.of(2026, 10, 15),
        status = RepairStatus.IN_PROGRESS,
        isSample = true
    ),
    RepairRequest(
        reference = "HR-2026-0001",
        room = Room.BATHROOM,
        issueType = IssueType.LEAK,
        description = "Water dripping from under the bathroom sink.",
        appointmentPreference = AppointmentPreference.MORNING,
        createdDate = LocalDate.of(2026, 9, 14),
        status = RepairStatus.COMPLETED,
        isSample = true
    )
)

/** Preview of the list with saved requests. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun MyRepairsScreenPreview() {
    MobileApplicationTheme {
        MyRepairsScreen(requests = previewRequests, onRequestClick = {}, onReportRepair = {}, onBack = {})
    }
}

/** Preview of the empty state. */
@Preview(showBackground = true, heightDp = 600)
@Composable
private fun MyRepairsScreenEmptyPreview() {
    MobileApplicationTheme {
        MyRepairsScreen(requests = emptyList(), onRequestClick = {}, onReportRepair = {}, onBack = {})
    }
}

/** Preview while the requests are loading. */
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun MyRepairsScreenLoadingPreview() {
    MobileApplicationTheme {
        MyRepairsScreen(requests = null, onRequestClick = {}, onReportRepair = {}, onBack = {})
    }
}
