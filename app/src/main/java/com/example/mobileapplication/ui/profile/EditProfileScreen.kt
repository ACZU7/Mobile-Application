package com.example.mobileapplication.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobileapplication.R
import com.example.mobileapplication.ui.shared.FieldError
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

/**
 * The Edit profile form. Every field is optional; errors are shown under the
 * field they belong to, as on the Housing repair form.
 *
 * Stateless: [form] comes from [ProfileViewModel].
 */
@Composable
fun EditProfileScreen(
    form: ProfileFormState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPostcodeChanged: (String) -> Unit,
    onVehicleChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.profile_edit_title), onBack = onCancel) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(Dimens.ScreenPadding)
            ) {
                DemoProfileNotice()
                Text(
                    text = stringResource(R.string.profile_edit_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ProfileField(
                    value = form.name,
                    onValueChange = onNameChanged,
                    label = R.string.profile_field_name,
                    placeholder = R.string.profile_placeholder_name,
                    icon = Icons.Filled.Person,
                    error = form.nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                ProfileField(
                    value = form.email,
                    onValueChange = onEmailChanged,
                    label = R.string.profile_field_email,
                    placeholder = R.string.profile_placeholder_email,
                    icon = Icons.Filled.Email,
                    error = form.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    )
                )
                ProfileField(
                    value = form.postcode,
                    onValueChange = onPostcodeChanged,
                    label = R.string.profile_field_postcode,
                    placeholder = R.string.profile_placeholder_postcode,
                    icon = Icons.Filled.LocationOn,
                    error = form.postcodeError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Next
                    )
                )
                ProfileField(
                    value = form.vehicleRegistration,
                    onValueChange = onVehicleChanged,
                    label = R.string.profile_field_vehicle,
                    placeholder = R.string.caz_plate_placeholder,
                    icon = Icons.Filled.DirectionsCar,
                    error = form.vehicleError,
                    supporting = R.string.profile_vehicle_hint,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSave() })
                )
                PrimaryButton(
                    text = stringResource(R.string.profile_save),
                    onClick = onSave,
                    modifier = Modifier.padding(top = Dimens.GroupSpacing)
                )
                SecondaryButton(text = stringResource(R.string.profile_cancel), onClick = onCancel)
            }
        }
    }
}

/** An outlined field with a leading icon and placeholder (the spec's OutlinedField), plus its error. */
@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    placeholder: Int,
    icon: ImageVector,
    error: Int?,
    keyboardOptions: KeyboardOptions,
    supporting: Int? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        placeholder = { Text(stringResource(placeholder)) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        isError = error != null,
        supportingText = when {
            error != null -> ({ FieldError(stringResource(error)) })
            supporting != null -> ({ Text(stringResource(supporting)) })
            else -> null
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EditProfilePreview(form: ProfileFormState, darkTheme: Boolean = false) {
    MobileApplicationTheme(darkTheme = darkTheme) {
        EditProfileScreen(form, {}, {}, {}, {}, onSave = {}, onCancel = {})
    }
}

/** An empty form, as for a new profile. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun EditProfileEmptyPreview() = EditProfilePreview(ProfileFormState())

/** A filled-in, valid form. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun EditProfileFilledPreview() = EditProfilePreview(
    ProfileFormState(name = "Sam Wilson", email = "sam.wilson@example.com", postcode = "BS1 5TR", vehicleRegistration = "IB15 PTL")
)

/** Errors on email, postcode and registration after tapping Save. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun EditProfileErrorsPreview() = EditProfilePreview(
    ProfileFormValidator.validate(
        ProfileFormState(name = "Sam", email = "sam@", postcode = "BS99", vehicleRegistration = "AB-12")
    )
)

/** The error state in dark mode. */
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun EditProfileErrorsDarkPreview() = EditProfilePreview(
    ProfileFormValidator.validate(ProfileFormState(email = "not an email", postcode = "12345")),
    darkTheme = true
)
