package com.example.mobileapplication.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.ui.theme.Dimens

// Pieces of Omar's original StaffScreen that more than one admin tab uses.
// The code is unchanged; it only moved here when the screen was split into tabs.

/**
 * The street report filters (status and type). The original single staff screen
 * kept one set of filters for its Reports, Map and Analytics views, so the admin
 * section keeps one [StaffFilters] (in a ViewModel scoped to the admin graph)
 * for the Reports and Analytics tabs.
 *
 * @property statusName The [StreetStatus] name to show, or null for all statuses.
 * @property categoryName The [StreetCategory] name to show, or null for all types.
 */
@Stable
class StaffFilters(statusName: String? = null, categoryName: String? = null) {
    var statusName by mutableStateOf(statusName)
    var categoryName by mutableStateOf(categoryName)

    /** The reports that match both filters (Omar's original filter). */
    fun apply(reports: List<StreetReport>) = reports.filter { (statusName == null || it.status.name == statusName) &&
        (categoryName == null || it.category.name == categoryName) }
}

/** Loading bar, error with Retry, and "Saved on this device." – shown on every staff tab, as before. */
internal fun LazyListScope.staffStatusItems(busy: Boolean, error: Boolean, saved: Boolean, onRetry: () -> Unit) {
    if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(stringResource(R.string.staff_loading)) }
    if (error) item {
        Text(stringResource(R.string.staff_error), color = MaterialTheme.colorScheme.error)
        OutlinedButton(onClick = onRetry, enabled = !busy) { Text(stringResource(R.string.staff_retry)) }
    }
    if (saved) item { Text(stringResource(R.string.staff_saved), color = MaterialTheme.colorScheme.primary) }
}

/** The status and type dropdowns, then "N reports" for the filtered list. */
internal fun LazyListScope.staffFilterItems(filters: StaffFilters, count: Int) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
            Box(Modifier.weight(1f)) { ChoiceMenu(filters.statusName?.let { stringResource(StreetStatus.valueOf(it).labelRes) }
                ?: stringResource(R.string.staff_all_status),
                listOf(null to stringResource(R.string.staff_all_status)) + StreetStatus.entries.map { it.name to stringResource(it.labelRes) },
                onSelect = { filters.statusName = it }) }
            Box(Modifier.weight(1f)) { ChoiceMenu(filters.categoryName?.let { stringResource(StreetCategory.valueOf(it).labelRes) }
                ?: stringResource(R.string.staff_all_types),
                listOf(null to stringResource(R.string.staff_all_types)) + StreetCategory.entries.map { it.name to stringResource(it.labelRes) },
                onSelect = { filters.categoryName = it }) }
        }
    }
    item { Text(stringResource(R.string.staff_count, count)) }
}

/**
 * An outlined button showing the current choice; tapping it opens a dropdown of
 * [choices] (value to label). Used for filters, status changes and car park categories.
 */
@Composable
internal fun ChoiceMenu(label: String, choices: List<Pair<String?, String>>, enabled: Boolean = true, onSelect: (String?) -> Unit) {
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
