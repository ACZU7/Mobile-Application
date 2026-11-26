package com.example.mobileapplication.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.*
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

/**
 * The admin Parking tab: Omar's editor for the fictional car parks (sample spaces,
 * price, EV charging and category), moved from the single staff screen.
 * Saved values update the resident car park finder on this device.
 *
 * @param vm Omar's [DashboardViewModel], shared by the admin tabs.
 */
@Composable
fun ParkingAdminScreen(vm: DashboardViewModel) {
    val parks by vm.carParks.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val saved by vm.saved.collectAsStateWithLifecycle()
    ParkingAdminContent(parks, busy, error, saved, onRetry = { vm.reload() }, onDismissNotice = { vm.dismissNotice() },
        onSave = { id, spaces, price, ev, category -> vm.updateParking(id, spaces, price, ev, category) })
}

/** The Parking tab's content, without the ViewModel, so it can be previewed. */
@Composable
fun ParkingAdminContent(
    parks: List<CarPark>,
    busy: Boolean,
    error: Boolean,
    saved: Boolean,
    onRetry: () -> Unit,
    onDismissNotice: () -> Unit,
    onSave: (String, Int, Double, Boolean, ParkingCategory) -> Unit
) {
    var editing by remember { mutableStateOf<CarPark?>(null) }
    LaunchedEffect(saved) { if (saved) editing = null }
    Scaffold(topBar = { TopBar(title = stringResource(R.string.staff_parking), onBack = null) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
            item { Text(stringResource(R.string.staff_demo), style = MaterialTheme.typography.bodySmall) }
            staffStatusItems(busy, error, saved, onRetry)
            item { Text(stringResource(R.string.staff_parking_notice)) }
            items(parks, key = { it.id }) { park ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Dimens.ScreenPadding), verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)) {
                        Text(stringResource(park.nameRes), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.parking_row_summary, park.sampleDistanceMiles, park.sampleSpaces))
                        Text(stringResource(R.string.parking_row_price, park.sampleHourlyPrice))
                        Text(stringResource(park.category.labelRes))
                        OutlinedButton(onClick = { onDismissNotice(); editing = park }, enabled = !busy) { Text(stringResource(R.string.staff_edit_parking)) }
                    }
                }
            }
        }
    }
    editing?.let { park -> ParkingEditDialog(park, busy, error, onDismiss = { if (!busy) editing = null }, onSave = { spaces, price, ev, category ->
        onDismissNotice(); onSave(park.id, spaces, price, ev, category)
    }) }
}

/**
 * Edits one car park's sample values. Spaces must be a whole number from 0 to
 * 10,000 and the price 0 to 100 with up to two decimal places; otherwise the
 * fields are marked and nothing is saved.
 */
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

/** Two fictional car parks for previews. */
private val previewCarParks = listOf(
    CarPark("sample-01", R.string.parking_name_01, ParkingType.CAR_PARK, ParkingArea.CENTRE, R.string.parking_address_01,
        R.string.parking_sample_opening, R.string.parking_sample_price, R.string.parking_sample_spaces, 51.454, -2.593,
        category = ParkingCategory.MULTI_STOREY, hasEvCharging = true),
    CarPark("sample-02", R.string.parking_name_02, ParkingType.CAR_PARK, ParkingArea.CENTRE, R.string.parking_address_02,
        R.string.parking_sample_opening, R.string.parking_sample_price, R.string.parking_sample_spaces, 51.452, -2.599,
        category = ParkingCategory.MULTI_STOREY, sampleDistanceMiles = 0.6, sampleSpaces = 450)
)

/** The car park list. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ParkingAdminPreview() = MobileApplicationTheme {
    ParkingAdminContent(previewCarParks, busy = false, error = false, saved = false, {}, {}, { _, _, _, _, _ -> })
}

/** Saving failed, in dark mode. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ParkingAdminErrorDarkPreview() = MobileApplicationTheme(darkTheme = true) {
    ParkingAdminContent(previewCarParks, busy = false, error = true, saved = false, {}, {}, { _, _, _, _, _ -> })
}
