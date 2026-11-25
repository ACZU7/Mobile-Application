package com.example.mobileapplication.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.ui.maps.*
import com.example.mobileapplication.ui.shared.*
import com.example.mobileapplication.ui.theme.Dimens
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(vm: DashboardViewModel, onBack: () -> Unit) {
    val reports by vm.reports.collectAsStateWithLifecycle()
    val parks by vm.carParks.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val saved by vm.saved.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var statusName by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<CarPark?>(null) }
    LaunchedEffect(saved) { if (saved) editing = null }
    val filtered = reports.filter { (statusName == null || it.status.name == statusName) &&
        (categoryName == null || it.category.name == categoryName) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.staff_title)) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.staff_back)) } },
        actions = { Icon(Icons.Default.AccountCircle, null, Modifier.padding(Dimens.ScreenPadding)) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
            item { Text(stringResource(R.string.staff_demo), style = MaterialTheme.typography.bodySmall) }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                    listOf(R.string.staff_reports, R.string.staff_map, R.string.staff_analytics, R.string.staff_parking).forEachIndexed { index, label ->
                        FilterChip(tab == index, { tab = index }, label = { Text(stringResource(label)) },
                            shape = androidx.compose.foundation.shape.CircleShape,
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
                    }
                }
            }
            if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(stringResource(R.string.staff_loading)) }
            if (error) item {
                Text(stringResource(R.string.staff_error), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { vm.reload() }, enabled = !busy) { Text(stringResource(R.string.staff_retry)) }
            }
            if (saved) item { Text(stringResource(R.string.staff_saved), color = MaterialTheme.colorScheme.primary) }
            if (tab != 3) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                        Box(Modifier.weight(1f)) { ChoiceMenu(statusName?.let { stringResource(StreetStatus.valueOf(it).labelRes) }
                            ?: stringResource(R.string.staff_all_status),
                            listOf(null to stringResource(R.string.staff_all_status)) + StreetStatus.entries.map { it.name to stringResource(it.labelRes) },
                            onSelect = { statusName = it }) }
                        Box(Modifier.weight(1f)) { ChoiceMenu(categoryName?.let { stringResource(StreetCategory.valueOf(it).labelRes) }
                            ?: stringResource(R.string.staff_all_types),
                            listOf(null to stringResource(R.string.staff_all_types)) + StreetCategory.entries.map { it.name to stringResource(it.labelRes) },
                            onSelect = { categoryName = it }) }
                    }
                }
                item { Text(stringResource(R.string.staff_count, filtered.size)) }
            }
            if (tab == 1 && filtered.isNotEmpty()) item {
                ReportMap(filtered)
                Text(stringResource(R.string.staff_map_notice), style = MaterialTheme.typography.bodySmall)
            }
            if (tab == 2) {
                item { Text(stringResource(R.string.staff_summary), style = MaterialTheme.typography.titleMedium) }
                items(StreetStatus.entries) { status ->
                    Text(stringResource(R.string.staff_metric, stringResource(status.labelRes), filtered.count { it.status == status }))
                }
                items(StreetCategory.entries) { category ->
                    Text(stringResource(R.string.staff_metric, stringResource(category.labelRes), filtered.count { it.category == category }))
                }
            } else if (tab == 3) {
                item { Text(stringResource(R.string.staff_parking_notice)) }
                items(parks, key = { it.id }) { park ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Dimens.ScreenPadding), verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                            Text(stringResource(park.nameRes), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.parking_row_summary, park.sampleDistanceMiles, park.sampleSpaces))
                            Text(stringResource(R.string.parking_row_price, park.sampleHourlyPrice))
                            Text(stringResource(park.category.labelRes))
                            OutlinedButton(onClick = { vm.dismissNotice(); editing = park }, enabled = !busy) { Text(stringResource(R.string.staff_edit_parking)) }
                        }
                    }
                }
            } else {
                if (filtered.isEmpty() && !busy) item { Text(stringResource(R.string.staff_empty)) }
                items(filtered, key = { it.reference }) { report ->
                    StaffReportCard(report, busy) { vm.updateStatus(report.reference, it) }
                }
            }
        }
    }
    editing?.let { park -> ParkingEditDialog(park, busy, error, onDismiss = { if (!busy) editing = null }, onSave = { spaces, price, ev, category ->
        vm.dismissNotice(); vm.updateParking(park.id, spaces, price, ev, category)
    }) }
}

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

@Composable
private fun ChoiceMenu(label: String, choices: List<Pair<String?, String>>, enabled: Boolean = true, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Text(label, Modifier.weight(1f)); Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(expanded, { expanded = false }) {
            choices.forEach { (value, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(value); expanded = false }) }
        }
    }
}

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

@Composable
private fun ParkingEditDialog(park: CarPark, busy: Boolean, error: Boolean, onDismiss: () -> Unit,
    onSave: (Int, Double, Boolean, ParkingCategory) -> Unit) {
    var spaces by rememberSaveable(park.id) { mutableStateOf(park.sampleSpaces.toString()) }
    var price by rememberSaveable(park.id) { mutableStateOf(java.lang.String.format(java.util.Locale.ROOT, "%.2f", park.sampleHourlyPrice)) }
    var ev by rememberSaveable(park.id) { mutableStateOf(park.hasEvCharging) }
    var category by rememberSaveable(park.id) { mutableStateOf(park.category.name) }
    var invalid by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.staff_edit_title)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
            Text(stringResource(park.nameRes))
            OutlinedTextField(spaces, { spaces = it }, label = { Text(stringResource(R.string.staff_spaces)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = invalid)
            OutlinedTextField(price, { price = it }, label = { Text(stringResource(R.string.staff_price)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = invalid)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.staff_ev), Modifier.weight(1f)); Switch(ev, { ev = it })
            }
            ChoiceMenu(stringResource(ParkingCategory.valueOf(category).labelRes),
                ParkingCategory.entries.map { it.name to stringResource(it.labelRes) }) { category = it!! }
            if (invalid) Text(stringResource(R.string.staff_invalid_parking), color = MaterialTheme.colorScheme.error)
            if (error) Text(stringResource(R.string.staff_error), color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(enabled = !busy, onClick = {
        val count = spaces.toIntOrNull(); val amount = price.toDoubleOrNull()
        if (count == null || count !in 0..10000 || amount == null || !amount.isFinite() || amount !in 0.0..100.0 ||
            !price.matches(Regex("[0-9]+(\\.[0-9]{1,2})?"))) invalid = true
        else onSave(count, amount, ev, ParkingCategory.valueOf(category))
    }) { Text(stringResource(if (busy) R.string.staff_saving else R.string.staff_update)) } }, dismissButton = {
        TextButton(onClick = onDismiss, enabled = !busy) { Text(stringResource(R.string.staff_cancel)) }
    })
}
