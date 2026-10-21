package com.example.mobileapplication.ui.housing

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.AppointmentPreference
import com.example.mobileapplication.data.model.IssueType
import com.example.mobileapplication.data.model.Room
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

/**
 * The housing repair form.
 *
 * This composable is "stateless": it draws whatever [state] it is given and
 * reports user actions through the callback parameters. It does not validate or
 * save anything itself; that is the job of [HousingViewModel].
 *
 * @param state The current form values and error messages.
 * @param onRoomSelected Called when the user picks a room.
 * @param onIssueTypeSelected Called when the user picks a problem type.
 * @param onDescriptionChanged Called whenever the description text changes.
 * @param onAppointmentSelected Called when the user picks an appointment preference.
 * @param onSubmit Called when the user taps the submit button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairFormScreen(
    state: RepairFormState,
    onRoomSelected: (Room) -> Unit,
    onIssueTypeSelected: (IssueType) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAppointmentSelected: (AppointmentPreference) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.housing_form_title)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.housing_form_intro),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = stringResource(R.string.housing_demo_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OptionDropdown(
                label = stringResource(R.string.housing_form_room_label),
                options = Room.entries,
                selected = state.room,
                optionLabel = { it.labelRes },
                onOptionSelected = onRoomSelected,
                errorRes = state.roomError
            )

            OptionDropdown(
                label = stringResource(R.string.housing_form_issue_label),
                options = IssueType.entries,
                selected = state.issueType,
                optionLabel = { it.labelRes },
                onOptionSelected = onIssueTypeSelected,
                errorRes = state.issueTypeError
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(R.string.housing_form_description_label)) },
                placeholder = { Text(stringResource(R.string.housing_form_description_hint)) },
                isError = state.descriptionError != null,
                supportingText = state.descriptionError?.let { { FieldError(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            AppointmentPreferenceGroup(
                selected = state.appointmentPreference,
                onSelected = onAppointmentSelected,
                errorRes = state.appointmentError
            )

            Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.housing_form_submit))
            }
        }
    }
}

/**
 * A read-only drop-down for choosing one value from a list of enum [options].
 *
 * The only state kept here is whether the menu is open ([expanded]). That is
 * purely visual, so it is fine to keep it inside the composable instead of the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> Int,
    onOptionSelected: (T) -> Unit,
    @StringRes errorRes: Int?
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { stringResource(optionLabel(it)) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(stringResource(R.string.housing_form_select_placeholder)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = errorRes != null,
            supportingText = errorRes?.let { { FieldError(it) } },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(optionLabel(option))) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * A group of radio buttons for the appointment preference.
 *
 * Each whole row is tappable (not just the small circle), which makes it easier to use.
 */
@Composable
private fun AppointmentPreferenceGroup(
    selected: AppointmentPreference?,
    onSelected: (AppointmentPreference) -> Unit,
    @StringRes errorRes: Int?
) {
    Column(modifier = Modifier.selectableGroup()) {
        Text(
            text = stringResource(R.string.housing_form_appointment_label),
            style = MaterialTheme.typography.titleSmall
        )
        AppointmentPreference.entries.forEach { preference ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = preference == selected,
                        onClick = { onSelected(preference) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp)
            ) {
                // onClick is null because the whole row already handles the tap.
                RadioButton(selected = preference == selected, onClick = null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(preference.labelRes))
            }
        }
        if (errorRes != null) {
            FieldError(errorRes)
        }
    }
}

/**
 * An error message shown under a form field.
 *
 * It uses a warning icon as well as the error colour, so the error is clear
 * even to users who cannot tell colours apart.
 */
@Composable
private fun FieldError(@StringRes messageRes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.housing_error_icon_description),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(messageRes),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** Preview of the empty form as the user first sees it. */
@Preview(showBackground = true)
@Composable
private fun RepairFormScreenEmptyPreview() {
    MobileApplicationTheme {
        RepairFormScreen(
            state = RepairFormState(),
            onRoomSelected = {},
            onIssueTypeSelected = {},
            onDescriptionChanged = {},
            onAppointmentSelected = {},
            onSubmit = {}
        )
    }
}

/** Preview of the form after submitting it empty, showing every error message. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RepairFormScreenErrorsPreview() {
    MobileApplicationTheme {
        RepairFormScreen(
            state = RepairFormValidator.validate(RepairFormState(description = "Leak")),
            onRoomSelected = {},
            onIssueTypeSelected = {},
            onDescriptionChanged = {},
            onAppointmentSelected = {},
            onSubmit = {}
        )
    }
}
