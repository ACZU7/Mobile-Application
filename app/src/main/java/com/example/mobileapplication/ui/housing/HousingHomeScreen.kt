package com.example.mobileapplication.ui.housing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

/**
 * The start of the Housing tab: a short list of housing services.
 *
 * - "Report a repair" opens the repair wizard.
 * - "My repairs" opens the list of saved repair requests.
 * - "Rent account" opens the sample rent account.
 *
 * @param repairCount How many repair requests are saved, or null while they are loading.
 * @param onReportRepair Called when "Report a repair" is tapped.
 * @param onMyRepairs Called when "My repairs" is tapped.
 * @param onRentAccount Called when "Rent account" is tapped.
 */
@Composable
fun HousingHomeScreen(
    repairCount: Int?,
    onReportRepair: () -> Unit,
    onMyRepairs: () -> Unit,
    onRentAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        // The Housing tab is opened from the bottom bar, so there is nothing to go back to.
        topBar = { TopBar(title = stringResource(R.string.housing_home_title), onBack = null) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(Dimens.ScreenPadding)
            ) {
                Text(
                    text = stringResource(R.string.housing_home_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HousingListRow(
                    icon = Icons.Filled.Build,
                    title = stringResource(R.string.housing_home_report_repair),
                    supportingText = stringResource(R.string.housing_home_report_repair_subtitle),
                    onClick = onReportRepair
                )
                HousingListRow(
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    title = stringResource(R.string.housing_home_my_repairs),
                    supportingText = when (repairCount) {
                        null -> stringResource(R.string.housing_loading)
                        0 -> stringResource(R.string.housing_home_my_repairs_none)
                        else -> pluralStringResource(R.plurals.housing_home_my_repairs_count, repairCount, repairCount)
                    },
                    onClick = onMyRepairs
                )
                HousingListRow(
                    icon = Icons.Filled.Home,
                    title = stringResource(R.string.housing_home_rent_account),
                    supportingText = stringResource(R.string.housing_home_rent_account_subtitle),
                    onClick = onRentAccount
                )

                Text(
                    text = stringResource(R.string.housing_demo_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.GroupSpacing)
                )
            }
        }
    }
}

/** Preview of the Housing home with saved repairs. */
@Preview(showBackground = true)
@Composable
private fun HousingHomeScreenPreview() {
    MobileApplicationTheme {
        HousingHomeScreen(repairCount = 4, onReportRepair = {}, onMyRepairs = {}, onRentAccount = {})
    }
}

/** Preview of the Housing home before any repairs exist. */
@Preview(showBackground = true)
@Composable
private fun HousingHomeScreenNoRepairsPreview() {
    MobileApplicationTheme {
        HousingHomeScreen(repairCount = 0, onReportRepair = {}, onMyRepairs = {}, onRentAccount = {})
    }
}

/** Preview of the Housing home while the repair count is loading. */
@Preview(showBackground = true)
@Composable
private fun HousingHomeScreenLoadingPreview() {
    MobileApplicationTheme {
        HousingHomeScreen(repairCount = null, onReportRepair = {}, onMyRepairs = {}, onRentAccount = {})
    }
}
