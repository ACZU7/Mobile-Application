package com.example.mobileapplication.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.RepairRequest
import com.example.mobileapplication.data.model.RepairStatus
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.data.repository.RepairRepository
import com.example.mobileapplication.ui.dashboard.ChoiceMenu
import com.example.mobileapplication.ui.housing.chipType
import com.example.mobileapplication.ui.housing.formatDate
import com.example.mobileapplication.ui.shared.StatusChip
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import java.time.LocalDate

/**
 * The admin Repairs tab: every housing repair request from the database, with
 * status and room filters, a count, and "Change status" with Update on each request.
 *
 * Stateless: it draws [state] from [RepairAdminViewModel] and reports actions.
 */
@Composable
fun RepairAdminScreen(
    state: RepairAdminUiState,
    onStatusFilterChanged: (RepairStatus?) -> Unit,
    onRoomFilterChanged: (Room?) -> Unit,
    onRetry: () -> Unit,
    onUpdateStatus: (String, RepairStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.admin_repairs_title), onBack = null) }
    ) { padding ->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val contentWidth = Modifier
                .widthIn(max = Dimens.MaxContentWidth)
                .fillMaxWidth()
            item {
                Text(
                    stringResource(R.string.admin_repairs_demo),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = contentWidth
                )
            }
            item { Filters(state, onStatusFilterChanged, onRoomFilterChanged, contentWidth) }
            item { Messages(state, onRetry, contentWidth) }
            when {
                state.isLoading -> Unit
                state.loadFailed -> Unit
                state.requests.isEmpty() -> item {
                    Text(stringResource(R.string.admin_repairs_empty), modifier = contentWidth)
                }
                else -> items(state.requests, key = { it.reference }) { request ->
                    RepairAdminCard(
                        request = request,
                        saving = state.savingReference == request.reference,
                        enabled = state.savingReference == null,
                        onUpdate = { onUpdateStatus(request.reference, it) },
                        modifier = contentWidth
                    )
                }
            }
        }
    }
}

/** The status and room dropdowns, and how many requests match. */
@Composable
private fun Filters(
    state: RepairAdminUiState,
    onStatusFilterChanged: (RepairStatus?) -> Unit,
    onRoomFilterChanged: (Room?) -> Unit,
    modifier: Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing), modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
            Box(Modifier.weight(1f)) {
                ChoiceMenu(
                    label = state.statusFilter?.let { stringResource(it.labelRes) } ?: stringResource(R.string.admin_repairs_all_statuses),
                    choices = listOf(null to stringResource(R.string.admin_repairs_all_statuses)) +
                        RepairStatus.entries.map { it.name to stringResource(it.labelRes) },
                    onSelect = { name -> onStatusFilterChanged(name?.let(RepairStatus::valueOf)) }
                )
            }
            Box(Modifier.weight(1f)) {
                ChoiceMenu(
                    label = state.roomFilter?.let { stringResource(it.labelRes) } ?: stringResource(R.string.admin_repairs_all_rooms),
                    choices = listOf(null to stringResource(R.string.admin_repairs_all_rooms)) +
                        Room.entries.map { it.name to stringResource(it.labelRes) },
                    onSelect = { name -> onRoomFilterChanged(name?.let(Room::valueOf)) }
                )
            }
        }
        if (!state.isLoading && !state.loadFailed) {
            Text(pluralStringResource(R.plurals.admin_repairs_count, state.requests.size, state.requests.size))
        }
    }
}

/** Loading, load error with Retry, save error, and "Saved on this device." Announced when they appear. */
@Composable
private fun Messages(state: RepairAdminUiState, onRetry: () -> Unit, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(stringResource(R.string.admin_repairs_loading))
        }
        if (state.loadFailed) {
            Text(stringResource(R.string.admin_repairs_load_error), color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.admin_repairs_retry)) }
        }
        if (state.saveFailed) {
            Text(stringResource(R.string.admin_repairs_save_error), color = MaterialTheme.colorScheme.error)
        }
        if (state.saved) {
            Text(stringResource(R.string.admin_repairs_saved), color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** One repair request with its details, status chip, and "Change status" with Update. */
@Composable
private fun RepairAdminCard(
    request: RepairRequest,
    saving: Boolean,
    enabled: Boolean,
    onUpdate: (RepairStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var next by rememberSaveable(request.reference, request.status) { mutableStateOf(request.status.name) }
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(Modifier.padding(Dimens.ScreenPadding), verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(request.issueType.labelRes), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.admin_repairs_reference_room, request.reference, stringResource(request.room.labelRes)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.admin_repairs_date, formatDate(request.createdDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(stringResource(request.status.labelRes), request.status.chipType)
            }
            Text(request.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.admin_repairs_appointment, stringResource(request.appointmentPreference.labelRes)),
                style = MaterialTheme.typography.bodySmall
            )
            if (request.isSample) {
                Text(
                    stringResource(R.string.housing_my_repairs_sample_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(stringResource(R.string.admin_repairs_change_status), style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                Box(Modifier.weight(1f)) {
                    ChoiceMenu(
                        label = stringResource(RepairStatus.valueOf(next).labelRes),
                        choices = RepairStatus.entries.map { it.name to stringResource(it.labelRes) },
                        enabled = enabled
                    ) { next = it!! }
                }
                Button(onClick = { onUpdate(RepairStatus.valueOf(next)) }, enabled = enabled && next != request.status.name) {
                    Text(stringResource(if (saving) R.string.admin_repairs_saving else R.string.admin_repairs_update))
                }
            }
        }
    }
}

private val previewRequests = RepairRepository.sampleRequests() + RepairRequest(
    reference = "HR-2026-0004",
    room = Room.LIVING_ROOM,
    issueType = IssueType.DOOR_WINDOW,
    description = "Window will not close properly.",
    appointmentPreference = AppointmentPreference.ANY_TIME,
    createdDate = LocalDate.of(2026, 11, 11),
    status = RepairStatus.SUBMITTED
)

@Composable
private fun RepairAdminPreview(state: RepairAdminUiState, darkTheme: Boolean = false) {
    MobileApplicationTheme(darkTheme = darkTheme) {
        RepairAdminScreen(state, {}, {}, {}, { _, _ -> })
    }
}

/** Every request. */
@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun RepairAdminListPreview() = RepairAdminPreview(
    RepairAdminUiState(isLoading = false, requests = previewRequests)
)

/** Filtered to the kitchen, after a status change was saved. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun RepairAdminSavedPreview() = RepairAdminPreview(
    RepairAdminUiState(
        isLoading = false,
        requests = previewRequests.filter { it.room == Room.KITCHEN },
        roomFilter = Room.KITCHEN,
        saved = true
    )
)

/** While a status change is saving. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun RepairAdminSavingPreview() = RepairAdminPreview(
    RepairAdminUiState(isLoading = false, requests = previewRequests.take(1), savingReference = previewRequests.first().reference)
)

/** Loading. */
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun RepairAdminLoadingPreview() = RepairAdminPreview(RepairAdminUiState(isLoading = true))

/** The list could not be read. */
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun RepairAdminLoadErrorPreview() = RepairAdminPreview(RepairAdminUiState(isLoading = false, loadFailed = true))

/** No requests match the filters, in dark mode. */
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun RepairAdminEmptyDarkPreview() = RepairAdminPreview(
    RepairAdminUiState(isLoading = false, statusFilter = RepairStatus.SUBMITTED, roomFilter = Room.HALLWAY),
    darkTheme = true
)

/** A status change could not be saved. */
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun RepairAdminSaveErrorPreview() = RepairAdminPreview(
    RepairAdminUiState(isLoading = false, requests = previewRequests.take(1), saveFailed = true)
)
