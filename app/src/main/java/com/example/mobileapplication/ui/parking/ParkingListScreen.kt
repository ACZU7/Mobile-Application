package com.example.mobileapplication.ui.parking

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.CarPark
import com.example.mobileapplication.data.model.ParkingArea
import com.example.mobileapplication.data.model.ParkingType
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.extendedColors

/** Screen 3: local illustration, fixed sample pins and an overlaid results sheet. */
@Composable
fun ParkingListScreen(
    state: ParkingUiState,
    onTypeSelected: (ParkingType?) -> Unit,
    onAreaSelected: (ParkingArea?) -> Unit,
    onClearFilters: () -> Unit,
    onFinderFilterSelected: (ParkingFinderFilter) -> Unit,
    onMapPinSelected: (String) -> Unit,
    onFiltersVisibleChanged: (Boolean) -> Unit,
    onLocationMessageVisibleChanged: (Boolean) -> Unit,
    onCarParkSelected: (String) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val sheetHeight = maxHeight * if (maxHeight < 440.dp) 0.43f else 0.40f
        StaticParkingMap(
            carParks = state.filteredCarParks,
            highlightedId = state.highlightedCarParkId,
            onPinSelected = onMapPinSelected,
            modifier = Modifier.fillMaxWidth().height(maxHeight - sheetHeight + 24.dp)
        )
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = Dimens.MaxContentWidth)
                .fillMaxWidth().padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.GroupSpacing)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = stringResource(R.string.parking_search_area), onValueChange = {},
                    readOnly = true, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                    IconButton(onClick = { onFiltersVisibleChanged(true) }) {
                        Icon(Icons.Default.Tune, contentDescription = stringResource(R.string.parking_filter_settings), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ParkingFinderFilter.entries.forEach { filter ->
                    ParkingFilterChip(state.finderFilter == filter, stringResource(filter.labelRes)) { onFinderFilterSelected(filter) }
                }
            }
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) {
                Text(
                    stringResource(R.string.parking_static_map_notice), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.GroupSpacing, vertical = 4.dp)
                )
            }
        }
        FloatingActionButton(
            onClick = { onLocationMessageVisibleChanged(true) }, shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = Dimens.ScreenPadding, bottom = sheetHeight + Dimens.CardSpacing).size(48.dp)
        ) { Icon(Icons.Default.Navigation, contentDescription = stringResource(R.string.parking_location_action)) }
        ParkingResultsSheet(state, onClearFilters, onCarParkSelected, Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(sheetHeight))
    }
    if (state.filtersVisible) {
        AlertDialog(
            onDismissRequest = { onFiltersVisibleChanged(false) },
            title = { Text(stringResource(R.string.parking_filter_settings)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.parking_filter_type), style = MaterialTheme.typography.titleSmall)
                    ParkingFilterChip(state.type == null, stringResource(R.string.parking_all_types)) { onTypeSelected(null) }
                    ParkingType.entries.forEach { type -> ParkingFilterChip(state.type == type, stringResource(type.labelRes)) { onTypeSelected(type) } }
                    Text(stringResource(R.string.parking_filter_area), style = MaterialTheme.typography.titleSmall)
                    ParkingFilterChip(state.area == null, stringResource(R.string.parking_all_areas)) { onAreaSelected(null) }
                    ParkingArea.entries.forEach { area -> ParkingFilterChip(state.area == area, stringResource(area.labelRes)) { onAreaSelected(area) } }
                }
            },
            confirmButton = { TextButton(onClick = { onFiltersVisibleChanged(false) }) { Text(stringResource(R.string.parking_apply_filters)) } },
            dismissButton = { TextButton(onClick = onClearFilters) { Text(stringResource(R.string.parking_clear_filters)) } }
        )
    }
    if (state.locationMessageVisible) {
        AlertDialog(
            onDismissRequest = { onLocationMessageVisibleChanged(false) },
            title = { Text(stringResource(R.string.parking_location_action)) },
            text = { Text(stringResource(R.string.parking_location_fallback)) },
            confirmButton = { TextButton(onClick = { onLocationMessageVisibleChanged(false) }) { Text(stringResource(R.string.parking_understood)) } }
        )
    }
}

@Composable
private fun ParkingFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        shape = CircleShape, border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.extendedColors.chipNeutral, labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun ParkingResultsSheet(state: ParkingUiState, onClearFilters: () -> Unit, onCarParkSelected: (String) -> Unit, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(6.dp))
            Box(Modifier.size(width = 32.dp, height = 3.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape))
            Column(Modifier.widthIn(max = Dimens.MaxContentWidth).fillMaxWidth().padding(horizontal = Dimens.ScreenPadding)) {
                Text(stringResource(R.string.parking_nearest), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = Dimens.GroupSpacing).semantics { heading() })
                Text(stringResource(R.string.parking_sheet_sample_notice), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp, bottom = 4.dp))
                LazyColumn(contentPadding = PaddingValues(bottom = Dimens.GroupSpacing)) {
                    if (state.isEmpty) {
                        item {
                            Text(stringResource(R.string.parking_empty_title), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.parking_empty_message), style = MaterialTheme.typography.bodySmall)
                            SecondaryButton(stringResource(R.string.parking_clear_filters), onClearFilters)
                        }
                    }
                    items(state.filteredCarParks, key = { it.id }) { carPark ->
                        ParkingResultRow(carPark) { onCarParkSelected(carPark.id) }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParkingResultRow(carPark: CarPark, onClick: () -> Unit) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = Dimens.GroupSpacing), horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.extendedColors.chipNeutral, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.LocalParking, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(carPark.nameRes), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(R.string.parking_row_summary, carPark.sampleDistanceMiles, carPark.sampleSpaces), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(stringResource(R.string.parking_row_price, carPark.sampleHourlyPrice), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
internal fun ParkingSampleNotice() {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.extendedColors.surfaceTint) {
        Text(stringResource(R.string.parking_sample_notice), modifier = Modifier.padding(Dimens.CardSpacing), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
