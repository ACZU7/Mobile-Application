package com.example.mobileapplication.ui.parking

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens

@Composable
fun ParkingDetailsScreen(
    state: ParkingUiState,
    onBack: () -> Unit,
    onDirections: () -> Unit,
    onDismissDirections: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(stringResource(R.string.parking_details_title), onBack) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = Dimens.MaxContentWidth).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)
            ) {
                ParkingSampleNotice()
                val carPark = state.selectedCarPark
                if (carPark == null) {
                    Text(stringResource(R.string.parking_missing_selection))
                    SecondaryButton(stringResource(R.string.parking_back_to_list), onBack)
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(carPark.nameRes),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(Dimens.ScreenPadding)
                        )
                    }
                    ParkingDetail(R.string.parking_filter_type, carPark.type.labelRes)
                    ParkingDetail(R.string.parking_filter_area, carPark.area.labelRes)
                    ParkingDetail(R.string.parking_address, carPark.addressRes)
                    ParkingDetail(R.string.parking_opening, carPark.openingInformationRes)
                    ParkingDetailText(R.string.parking_price, stringResource(R.string.parking_details_sample_price, carPark.sampleHourlyPrice))
                    ParkingDetailText(R.string.parking_spaces, stringResource(R.string.parking_details_sample_spaces, carPark.sampleSpaces))
                    PrimaryButton(stringResource(R.string.parking_directions), onDirections)
                }
            }
        }
    }
    if (state.directionsMessageVisible && state.selectedCarPark != null) {
        AlertDialog(
            onDismissRequest = onDismissDirections,
            title = { Text(stringResource(R.string.parking_directions)) },
            text = { Text(stringResource(R.string.parking_directions_fallback)) },
            confirmButton = {
                TextButton(onClick = onDismissDirections) { Text(stringResource(R.string.parking_understood)) }
            }
        )
    }
}

@Composable
private fun ParkingDetail(@StringRes label: Int, @StringRes value: Int) {
    ParkingDetailText(label, stringResource(value))
}

@Composable
private fun ParkingDetailText(@StringRes label: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
        Text(stringResource(label), style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
