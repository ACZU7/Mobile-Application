package com.example.mobileapplication.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

/**
 * The admin Analytics tab: Omar's local street report summary (counts by status
 * and by type for the filtered reports, moved from the single staff screen).
 *
 * @param vm Omar's [DashboardViewModel], shared by the admin tabs.
 * @param filters The status and type filters, shared with the Reports tab.
 */
@Composable
fun AnalyticsScreen(vm: DashboardViewModel, filters: StaffFilters) {
    val reports by vm.reports.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val saved by vm.saved.collectAsStateWithLifecycle()
    AnalyticsContent(reports, busy, error, saved, filters, onRetry = { vm.reload() })
}

/** The Analytics tab's content, without the ViewModel, so it can be previewed. */
@Composable
fun AnalyticsContent(
    reports: List<StreetReport>,
    busy: Boolean,
    error: Boolean,
    saved: Boolean,
    filters: StaffFilters,
    onRetry: () -> Unit
) {
    val filtered = filters.apply(reports)
    Scaffold(topBar = { TopBar(title = stringResource(R.string.staff_analytics), onBack = null) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
            item { Text(stringResource(R.string.staff_demo), style = MaterialTheme.typography.bodySmall) }
            staffStatusItems(busy, error, saved, onRetry)
            staffFilterItems(filters, filtered.size)
            item { Text(stringResource(R.string.staff_summary), style = MaterialTheme.typography.titleMedium) }
            items(StreetStatus.entries) { status ->
                Text(stringResource(R.string.staff_metric, stringResource(status.labelRes), filtered.count { it.status == status }))
            }
            items(StreetCategory.entries) { category ->
                Text(stringResource(R.string.staff_metric, stringResource(category.labelRes), filtered.count { it.category == category }))
            }
        }
    }
}

/** The street report summary. */
@Preview(showBackground = true, heightDp = 1300)
@Composable
private fun AnalyticsPreview() = MobileApplicationTheme {
    AnalyticsContent(previewStreetReports, busy = false, error = false, saved = false, StaffFilters(), onRetry = {})
}

/** Loading, in dark mode. */
@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun AnalyticsLoadingDarkPreview() = MobileApplicationTheme(darkTheme = true) {
    AnalyticsContent(previewStreetReports, busy = true, error = false, saved = false, StaffFilters(), onRetry = {})
}
