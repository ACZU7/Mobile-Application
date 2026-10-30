package com.example.mobileapplication.ui.streetissues

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.ui.shared.*
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.extendedColors

@Composable
private fun StreetPage(title: Int, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { TopBar(stringResource(title), onBack) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = Dimens.MaxContentWidth).fillMaxWidth()
                .verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
                Text(stringResource(R.string.street_demo), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                content()
            }
        }
    }
}

@Composable
fun StreetFormScreen(state: StreetFormState, onCategory: (StreetCategory) -> Unit,
    onDescription: (String) -> Unit, onLocation: (String) -> Unit,
    onSubmit: () -> Unit, onReports: () -> Unit, onBack: () -> Unit) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }
    StreetPage(R.string.street_title, onBack) {
        SecondaryButton(stringResource(R.string.street_reports), onReports)
        Text(stringResource(R.string.street_category), style = MaterialTheme.typography.titleSmall)
        StreetCategory.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                pair.forEach { category ->
                    FilterChip(selected = state.category == category, onClick = { onCategory(category) },
                        label = { Text(stringResource(category.labelRes)) }, modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.extendedColors.chipNeutral,
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary))
                }
            }
        }
        state.categoryError?.let { StreetError(it) }
        OutlinedCard {
            Row(Modifier.padding(Dimens.ScreenPadding), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.street_photo), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(stringResource(R.string.street_location), style = MaterialTheme.typography.titleSmall)
        OutlinedCard {
            SampleStreetMap()
            Text(state.locationLabel, Modifier.padding(Dimens.CardSpacing), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { draft = state.locationLabel; editing = true }) {
                Icon(Icons.Default.Edit, null)
                Text(stringResource(R.string.street_edit_location))
            }
        }
        state.locationError?.let { StreetError(it) }
        OutlinedTextField(value = state.description, onValueChange = onDescription,
            label = { Text(stringResource(R.string.street_description)) },
            isError = state.descriptionError != null, minLines = 3,
            supportingText = {
                Column {
                    state.descriptionError?.let { StreetError(it) }
                    Text(stringResource(R.string.street_counter, state.description.length))
                }
            }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small)
        Text(stringResource(R.string.street_disclaimer), style = MaterialTheme.typography.bodySmall)
        PrimaryButton(stringResource(R.string.street_submit), onSubmit)
    }
    if (editing) AlertDialog(onDismissRequest = { editing = false },
        title = { Text(stringResource(R.string.street_edit_location)) },
        text = { OutlinedTextField(value = draft, onValueChange = { draft = it },
            label = { Text(stringResource(R.string.street_location_label)) },
            isError = draft.isBlank(), supportingText = {
                if (draft.isBlank()) StreetError(R.string.street_error_location)
            }) },
        confirmButton = { TextButton(enabled = draft.isNotBlank(), onClick = { onLocation(draft); editing = false }) {
            Text(stringResource(R.string.street_save_location))
        } }, dismissButton = { TextButton(onClick = { editing = false }) { Text(stringResource(R.string.street_cancel)) } })
}

@Composable
private fun StreetError(message: Int) {
    Text(stringResource(message), color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

@Composable
private fun SampleStreetMap() {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.street_map_description)
    Box(Modifier.fillMaxWidth().height(Dimens.ButtonHeight * 2).semantics { contentDescription = description },
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(colors.surfaceVariant)
            for (i in 0..5) {
                val x = size.width * i / 5
                drawLine(colors.surface, Offset(x, 0f), Offset(x + size.width / 4, size.height), strokeWidth = size.height / 12)
            }
            drawLine(colors.surface, Offset(0f, size.height / 2), Offset(size.width, size.height / 3), strokeWidth = size.height / 8)
        }
        Icon(Icons.Default.LocationOn, null, tint = colors.secondary, modifier = Modifier.size(Dimens.ButtonHeight))
    }
}

@Composable
private fun ReportSummary(report: StreetReport) {
    Text(stringResource(report.category.labelRes), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.street_reference, report.reference), style = MaterialTheme.typography.bodyMedium)
    Text(stringResource(R.string.street_date, report.createdDate.toString()), style = MaterialTheme.typography.bodySmall)
    Text(report.description, style = MaterialTheme.typography.bodyMedium)
    Text(report.locationLabel, style = MaterialTheme.typography.bodyMedium)
    StatusChip(stringResource(report.status.labelRes), when (report.status) {
        StreetStatus.SUBMITTED -> StatusChipType.SUBMITTED
        StreetStatus.IN_PROGRESS -> StatusChipType.IN_PROGRESS
        StreetStatus.FIXED -> StatusChipType.FIXED
    })
}

@Composable
private fun ReportTimeline(report: StreetReport) {
    Stepper(items = StreetStatus.entries.map { status ->
        StepperItem(stringResource(status.labelRes), when {
            status.ordinal < report.status.ordinal -> StepperState.DONE
            status == report.status -> StepperState.CURRENT
            else -> StepperState.UPCOMING
        })
    })
}

@Composable
fun StreetConfirmationScreen(report: StreetReport?, onStatus: () -> Unit, onAnother: () -> Unit, onBack: () -> Unit) {
    StreetPage(R.string.street_saved, onBack) {
        Text(stringResource(R.string.street_disclaimer), style = MaterialTheme.typography.titleSmall)
        if (report != null) {
            ReportSummary(report)
            PrimaryButton(stringResource(R.string.street_view_status), onStatus)
        } else Text(stringResource(R.string.street_missing))
        SecondaryButton(stringResource(R.string.street_another), onAnother)
    }
}

@Composable
fun StreetReportsScreen(reports: List<StreetReport>, filter: StreetStatus?, onFilter: (StreetStatus?) -> Unit,
    onReport: (String) -> Unit, onBack: () -> Unit) {
    StreetPage(R.string.street_reports, onBack) {
        // Two rows keep all four filters usable at larger font sizes and in RTL.
        listOf(listOf(null, StreetStatus.SUBMITTED), listOf(StreetStatus.IN_PROGRESS, StreetStatus.FIXED)).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                pair.forEach { status ->
                    FilterChip(selected = filter == status, onClick = { onFilter(status) },
                        label = { Text(stringResource(status?.labelRes ?: R.string.street_all)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.extendedColors.chipNeutral,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
                }
            }
        }
        val filtered = StreetIssuesViewModel.filtered(reports, filter)
        if (filtered.isEmpty()) Text(stringResource(R.string.street_empty))
        filtered.forEachIndexed { index, report ->
            OutlinedCard(onClick = { onReport(report.reference) }, modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(Dimens.CardSpacing), verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                    ReportSummary(report)
                    if (index == 0) ReportTimeline(report)
                }
            }
        }
    }
}

@Composable
fun StreetStatusScreen(report: StreetReport?, onReports: () -> Unit, onBack: () -> Unit) {
    StreetPage(R.string.street_status_title, onBack) {
        Text(stringResource(R.string.street_disclaimer), style = MaterialTheme.typography.titleSmall)
        if (report != null) {
            ReportSummary(report)
            ReportTimeline(report)
        } else Text(stringResource(R.string.street_missing))
        SecondaryButton(stringResource(R.string.street_reports), onReports)
    }
}
