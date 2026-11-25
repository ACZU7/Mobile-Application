package com.example.mobileapplication.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.domain.cleanairzone.RegistrationValidator
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.TopBar
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import com.example.mobileapplication.ui.theme.extendedColors

/**
 * The Profile tab (the profile part of mockup screen 12): an avatar with
 * initials, the saved details, an Edit button, the demo notice and "Clear my data".
 *
 * Stateless: it draws [state] and reports taps through the callbacks.
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEdit: () -> Unit,
    onClearRequested: () -> Unit,
    onClearDismissed: () -> Unit,
    onClearConfirmed: () -> Unit,
    onMessageShown: () -> Unit,
    onAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.message?.let { stringResource(it.textRes) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = stringResource(R.string.profile_title), onBack = null) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val profile = state.profile ?: return@Column
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing),
                modifier = Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .padding(Dimens.ScreenPadding)
            ) {
                ProfileHeader(profile)
                DemoProfileNotice()
                Text(
                    text = stringResource(R.string.profile_details_heading),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(top = Dimens.GroupSpacing)
                        .semantics { heading() }
                )
                ProfileDetailsCard(profile)
                Spacer(Modifier.heightIn(min = Dimens.GroupSpacing))
                PrimaryButton(
                    text = stringResource(if (profile.isEmpty) R.string.profile_set_up else R.string.profile_edit),
                    onClick = onEdit
                )
                // Omar's local Staff dashboard stays accessible inside Mohammad's Profile.
                SecondaryButton(
                    text = stringResource(R.string.profile_admin_entry),
                    onClick = onAdmin
                )
                TextButton(
                    onClick = onClearRequested,
                    enabled = !profile.isEmpty,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.ButtonHeight)
                ) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Dimens.GroupSpacing))
                    Text(stringResource(R.string.profile_clear), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (state.showClearDialog) {
        ClearDataDialog(onConfirm = onClearConfirmed, onDismiss = onClearDismissed)
    }
}

/** The avatar with the name and email beside it, as at the top of mockup 12. */
@Composable
private fun ProfileHeader(profile: UserProfile) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileAvatar(initials = profile.initials)
        Spacer(Modifier.width(Dimens.ScreenPadding))
        Column(Modifier.weight(1f)) {
            Text(
                text = profile.name ?: stringResource(R.string.profile_no_name),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = profile.email ?: stringResource(R.string.profile_header_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A round avatar showing the initials, or a person icon when there is no name. */
@Composable
fun ProfileAvatar(initials: String?, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(72.dp)
            .background(MaterialTheme.extendedColors.surfaceTint, CircleShape)
            .clearAndSetSemantics {}
    ) {
        if (initials != null) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

/** "Demo profile, stored only on this phone. Not a council account." */
@Composable
fun DemoProfileNotice(modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.extendedColors.surfaceTint,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Dimens.CardSpacing)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(Dimens.GroupSpacing))
            Text(text = stringResource(R.string.profile_demo_notice), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** The card listing name, email, postcode and saved vehicle, with "Not set" for empty ones. */
@Composable
private fun ProfileDetailsCard(profile: UserProfile) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailRow(Icons.Filled.Person, stringResource(R.string.profile_field_name), profile.name)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailRow(Icons.Filled.Email, stringResource(R.string.profile_field_email), profile.email)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailRow(Icons.Filled.LocationOn, stringResource(R.string.profile_field_postcode), profile.postcode)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        DetailRow(
            Icons.Filled.DirectionsCar,
            stringResource(R.string.profile_field_vehicle),
            profile.vehicleRegistration?.let(RegistrationValidator::format)
        )
    }
}

/** One row of the details card. Read out as "Email, sam@example.com". */
@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String?) {
    val shown = value ?: stringResource(R.string.profile_not_set)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.GroupSpacing)
            .clearAndSetSemantics { contentDescription = "$label, $shown" }
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(Dimens.CardSpacing))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = shown,
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Asks before deleting, because clearing cannot be undone. */
@Composable
private fun ClearDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null) },
        title = { Text(stringResource(R.string.profile_clear_dialog_title)) },
        text = { Text(stringResource(R.string.profile_clear_dialog_body)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(R.string.profile_clear_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_clear_dialog_cancel)) }
        }
    )
}

private val ProfileMessage.textRes: Int
    get() = when (this) {
        ProfileMessage.SAVED -> R.string.profile_message_saved
        ProfileMessage.CLEARED -> R.string.profile_message_cleared
        ProfileMessage.SAVE_FAILED -> R.string.profile_message_save_failed
        ProfileMessage.CLEAR_FAILED -> R.string.profile_message_clear_failed
    }

private val previewProfile = UserProfile(
    name = "Sam Wilson",
    email = "sam.wilson@example.com",
    postcode = "BS1 5TR",
    vehicleRegistration = "IB15PTL"
)

@Composable
private fun ProfilePreview(state: ProfileUiState, darkTheme: Boolean = false) {
    MobileApplicationTheme(darkTheme = darkTheme) {
        ProfileScreen(state, onEdit = {}, onClearRequested = {}, onClearDismissed = {}, onClearConfirmed = {}, onMessageShown = {}, onAdmin = {})
    }
}

/** A filled-in profile. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ProfileFilledPreview() = ProfilePreview(ProfileUiState(profile = previewProfile))

/** Nothing saved yet: person icon, "Not set" rows and "Set up profile". */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ProfileEmptyPreview() = ProfilePreview(ProfileUiState(profile = UserProfile()))

/** Only a name saved, in dark mode. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ProfilePartialDarkPreview() =
    ProfilePreview(ProfileUiState(profile = UserProfile(name = "Sam")), darkTheme = true)

/** The "Clear my data" confirmation. */
@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ProfileClearDialogPreview() =
    ProfilePreview(ProfileUiState(profile = previewProfile, showClearDialog = true))

/** Large font (200%). */
@Preview(showBackground = true, heightDp = 900, fontScale = 2f)
@Composable
private fun ProfileLargeFontPreview() = ProfilePreview(ProfileUiState(profile = previewProfile))
