package com.example.mobileapplication.ui.streetissues

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.ui.shared.*
import com.example.mobileapplication.data.repository.StreetReportRepository
import com.example.mobileapplication.ui.maps.MapMarkers
import com.example.mobileapplication.ui.maps.OsmMap
import com.example.mobileapplication.ui.maps.addMarker
import com.example.mobileapplication.ui.maps.frame
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint as OsmPoint
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
    onAddPhoto: () -> Unit, onRemovePhoto: (String) -> Unit,
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
        Text(stringResource(R.string.street_photos_heading), style = MaterialTheme.typography.titleSmall)
        StreetPhotoRow(state.photos, state.canAddPhoto, onAddPhoto, onRemovePhoto)
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

/** Small fixed OpenStreetMap preview with the report pin; not draggable, so the form scrolls normally. */
@Composable
private fun SampleStreetMap() {
    val context = LocalContext.current
    val colours = MaterialTheme.colorScheme
    val pin = colours.secondary.toArgb()
    val ring = colours.surface.toArgb()
    val point = OsmPoint(StreetReportRepository.SAMPLE_LATITUDE, StreetReportRepository.SAMPLE_LONGITUDE)
    OsmMap(stringResource(R.string.street_map_description), Modifier.fillMaxWidth().height(Dimens.ButtonHeight * 3), interactive = false) { map ->
        if (map.overlays.none { it is Marker }) {
            val icon = MapMarkers.pin(context, pin, ring, null, ring)
            map.addMarker(point, icon, MapMarkers.pinAnchorY(icon, context))
            map.frame(listOf(point), singlePointZoom = 16.0)
        }
    }
}

@Composable
private fun ReportSummary(report: StreetReport) {
    report.imageUris.firstOrNull()?.let { photo ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
            StreetPhoto(photo, Modifier.size(Dimens.ButtonHeight * 1.5f))
            if (report.imageUris.size > 1) {
                Text(stringResource(R.string.street_photo_count, report.imageUris.size), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
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

/** Mockup screen 6: photo thumbnails with a remove button, then an add tile while fewer than the maximum. */
@Composable
private fun StreetPhotoRow(photos: List<String>, canAdd: Boolean, onAdd: () -> Unit, onRemove: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
        photos.forEachIndexed { index, photo ->
            Box(Modifier.size(width = PhotoTileWidth, height = PhotoTileHeight)) {
                StreetPhoto(photo, Modifier.fillMaxSize(), stringResource(R.string.street_photo_description, index + 1))
                val removeDescription = stringResource(R.string.street_photo_remove, index + 1)
                Surface(onClick = { onRemove(photo) }, shape = CircleShape, color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(Dimens.ButtonHeight)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, removeDescription, Modifier.size(20.dp)) }
                }
            }
        }
        if (canAdd) {
            Surface(onClick = onAdd, shape = MaterialTheme.shapes.medium, color = MaterialTheme.extendedColors.chipNeutral,
                modifier = Modifier.size(width = PhotoTileWidth, height = PhotoTileHeight)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(Dimens.GroupSpacing)) {
                    Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(if (photos.isEmpty()) R.string.street_photo_add else R.string.street_photo_add_another),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                }
            }
        }
    }
    Text(stringResource(R.string.street_photo_hint, STREET_MAX_PHOTOS), style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** A stored street photo. Coil reads it off the main thread and applies the camera's EXIF rotation. */
@Composable
private fun StreetPhoto(uri: String, modifier: Modifier, description: String? = null) {
    AsyncImage(model = uri, contentDescription = description, contentScale = ContentScale.Crop,
        modifier = modifier.clip(MaterialTheme.shapes.medium).background(MaterialTheme.extendedColors.chipNeutral))
}

private val PhotoTileWidth = 150.dp
private val PhotoTileHeight = 112.dp
