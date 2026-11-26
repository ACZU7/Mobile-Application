package com.example.mobileapplication.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.ui.maps.*
import com.example.mobileapplication.ui.shared.*
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.time.LocalDate

/**
 * The admin Reports tab: Omar's street report list with its status and type
 * filters and "Change status", moved from the single staff screen. A List/Map
 * switch replaces the old "Map" chip; Map shows his report map above the same list.
 *
 * @param vm Omar's [DashboardViewModel], shared by the admin tabs.
 * @param filters The status and type filters, shared with the Analytics tab.
 */
@Composable
fun ReportsScreen(vm: DashboardViewModel, filters: StaffFilters) {
    val reports by vm.reports.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val saved by vm.saved.collectAsStateWithLifecycle()
    ReportsContent(reports, busy, error, saved, filters, onRetry = { vm.reload() },
        onUpdate = { reference, status -> vm.updateStatus(reference, status) })
}

/** The Reports tab's content, without the ViewModel, so it can be previewed. */
@Composable
fun ReportsContent(
    reports: List<StreetReport>,
    busy: Boolean,
    error: Boolean,
    saved: Boolean,
    filters: StaffFilters,
    onRetry: () -> Unit,
    onUpdate: (String, StreetStatus) -> Unit
) {
    var showMap by rememberSaveable { mutableStateOf(false) }
    val filtered = filters.apply(reports)
    Scaffold(topBar = { TopBar(title = stringResource(R.string.staff_title), onBack = null) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
            item { Text(stringResource(R.string.staff_demo), style = MaterialTheme.typography.bodySmall) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                    listOf(false to R.string.staff_list, true to R.string.staff_map).forEach { (map, label) ->
                        FilterChip(showMap == map, { showMap = map }, label = { Text(stringResource(label)) },
                            shape = androidx.compose.foundation.shape.CircleShape,
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
                    }
                }
            }
            staffStatusItems(busy, error, saved, onRetry)
            staffFilterItems(filters, filtered.size)
            if (showMap && filtered.isNotEmpty()) item {
                ReportMap(filtered)
                Text(stringResource(R.string.staff_map_notice), style = MaterialTheme.typography.bodySmall)
            }
            if (filtered.isEmpty() && !busy) item { Text(stringResource(R.string.staff_empty)) }
            items(filtered, key = { it.reference }) { report ->
                StaffReportCard(report, busy) { onUpdate(report.reference, it) }
            }
        }
    }
}

/** One street report: photo or icon, details, status chip, and "Change status" with Update. */
@Composable
private fun StaffReportCard(report: StreetReport, busy: Boolean, onUpdate: (StreetStatus) -> Unit) {
    var next by rememberSaveable(report.reference, report.status) { mutableStateOf(report.status.name) }
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Dimens.ScreenPadding), verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                if (report.imageUris.isNotEmpty()) AsyncImage(report.imageUris.first(), stringResource(R.string.staff_photo),
                    Modifier.size(64.dp), contentScale = ContentScale.Crop)
                else Icon(Icons.Default.ReportProblem, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.secondary)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(report.category.labelRes), style = MaterialTheme.typography.titleMedium)
                    Text(report.locationLabel, style = MaterialTheme.typography.bodySmall)
                    Text(report.reference, style = MaterialTheme.typography.bodySmall)
                    Text(report.createdDate.toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
            StatusChip(stringResource(report.status.labelRes), StatusChipType.valueOf(report.status.name))
            Text(report.description)
            Text(stringResource(R.string.staff_status), style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                Box(Modifier.weight(1f)) { ChoiceMenu(stringResource(StreetStatus.valueOf(next).labelRes),
                    StreetStatus.entries.map { it.name to stringResource(it.labelRes) }, !busy) { next = it!! } }
                Button(onClick = { onUpdate(StreetStatus.valueOf(next)) }, enabled = !busy && next != report.status.name) {
                    Text(stringResource(R.string.staff_update))
                }
            }
        }
    }
}

/** The filtered reports as pins on a non-interactive OpenStreetMap. */
@Composable
private fun ReportMap(reports: List<StreetReport>) {
    val fill = MaterialTheme.colorScheme.secondary.toArgb()
    val outline = MaterialTheme.colorScheme.surface.toArgb()
    OsmMap(stringResource(R.string.staff_map_description), Modifier.fillMaxWidth().height(280.dp), interactive = false) { map ->
        map.overlays.removeAll { it is Marker }
        reports.forEach { report -> map.addMarker(GeoPoint(report.latitude, report.longitude),
            MapMarkers.pin(map.context, fill, outline, null, outline), Marker.ANCHOR_BOTTOM) }
        map.frame(reports.map { GeoPoint(it.latitude, it.longitude) }); map.invalidate()
    }
}

/** Two fictional reports for previews, like the repository's samples. */
internal val previewStreetReports = listOf(
    StreetReport("SR-2026-0003", StreetCategory.POTHOLE, "Fictional pothole near a sample crossing.", "Demo Nelson Street, Bristol",
        51.457, -2.592, createdDate = LocalDate.of(2026, 10, 20), status = StreetStatus.IN_PROGRESS),
    StreetReport("SR-2026-0002", StreetCategory.STREET_LIGHT, "Fictional street light awaiting inspection.", "Demo Park Street, Bristol",
        51.457, -2.592, createdDate = LocalDate.of(2026, 10, 18), status = StreetStatus.SUBMITTED)
)

/** The report list. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReportsListPreview() = MobileApplicationTheme {
    ReportsContent(previewStreetReports, busy = false, error = false, saved = false, StaffFilters(), {}, { _, _ -> })
}

/** After a status change was saved. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReportsSavedPreview() = MobileApplicationTheme {
    ReportsContent(previewStreetReports, busy = false, error = false, saved = true, StaffFilters(), {}, { _, _ -> })
}

/** Loading, and a load error with Retry. */
@Preview(showBackground = true, heightDp = 600)
@Composable
private fun ReportsLoadingErrorPreview() = MobileApplicationTheme {
    ReportsContent(emptyList(), busy = true, error = true, saved = false, StaffFilters(), {}, { _, _ -> })
}

/** No reports match the filters, in dark mode. */
@Preview(showBackground = true, heightDp = 600)
@Composable
private fun ReportsEmptyDarkPreview() = MobileApplicationTheme(darkTheme = true) {
    ReportsContent(previewStreetReports, busy = false, error = false, saved = false,
        StaffFilters(statusName = StreetStatus.FIXED.name), {}, { _, _ -> })
}
