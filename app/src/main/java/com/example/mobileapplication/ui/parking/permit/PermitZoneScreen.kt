package com.example.mobileapplication.ui.parking.permit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.toggleable
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.ResidentPermit
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.extendedColors
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Mockup screen 4: permit zone checker with the stored resident's permit and expiry reminder. */
@Composable
fun PermitZoneScreen(
    state: PermitUiState,
    onBack: () -> Unit,
    onReminderChanged: (Boolean) -> Unit,
    onZoneSelected: () -> Unit,
    onPermitSelected: () -> Unit,
    onDismissDialog: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val sheetHeight = maxHeight * if (maxHeight < 480.dp) 0.62f else 0.50f
        PermitZoneMap(state.zone.code, Modifier.fillMaxWidth().height(maxHeight - sheetHeight + 24.dp))
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = Dimens.MaxContentWidth).fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.GroupSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)
        ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.shared_back), tint = MaterialTheme.colorScheme.primary)
                }
            }
            OutlinedTextField(
                value = stringResource(R.string.permit_sample_address), onValueChange = {},
                readOnly = true, singleLine = true, shape = CircleShape,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            )
        }
        // The zone is drawn over a real neighbourhood, so it must be obvious that the boundary is invented.
        Surface(
            shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.secondary)
        ) {
            Row(
                Modifier.padding(horizontal = Dimens.CardSpacing, vertical = Dimens.GroupSpacing),
                horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Text(
                    stringResource(R.string.permit_map_sample_banner, state.zone.code),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(sheetHeight),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.TopCenter) {
                Column(
                    Modifier.widthIn(max = Dimens.MaxContentWidth).fillMaxWidth().verticalScroll(rememberScrollState())
                        .padding(Dimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)
                ) {
                    ZoneResultCard(state, onZoneSelected)
                    Text(
                        stringResource(R.string.permit_your_permit), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() }
                    )
                    val permit = state.permit
                    if (permit == null) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    } else {
                        PermitCard(permit, onPermitSelected)
                        ReminderRow(permit, state.reminderEnabled, onReminderChanged)
                    }
                    Text(
                        stringResource(R.string.permit_sample_notice), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    PermitDialogs(state, onDismissDialog, onOpenNotificationSettings)
}

@Composable
private fun ZoneResultCard(state: PermitUiState, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.extendedColors.statusNew.container, contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(Dimens.CardSpacing), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
            Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.parking_pin_letter), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSecondary)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.permit_inside_zone, state.zone.code), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
                )
                Text(stringResource(state.zone.hoursRes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.permit_zone_info_action), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermitCard(permit: ResidentPermit, onClick: () -> Unit) {
    Surface(
        onClick = onClick, shape = MaterialTheme.shapes.medium, color = MaterialTheme.extendedColors.surfaceTint,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(Dimens.CardSpacing), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)) {
            Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.permit_card_title, permit.zoneCode), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.permit_expires, formatDate(permit)), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.permit_details_action), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReminderRow(permit: ResidentPermit, enabled: Boolean, onChanged: (Boolean) -> Unit) {
    val stateText = stringResource(if (enabled) R.string.permit_reminder_on else R.string.permit_reminder_off)
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .toggleable(value = enabled, role = Role.Switch, onValueChange = onChanged)
                .semantics { stateDescription = stateText },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.permit_remind_me), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(
                checked = enabled, onCheckedChange = null,
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        }
        val reminderDate = ReminderTiming.reminderTime(permit.expiryDate).toLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        Text(
            if (enabled) stringResource(R.string.permit_reminder_scheduled, reminderDate)
            else stringResource(R.string.permit_reminder_explainer, ReminderTiming.DAYS_BEFORE_EXPIRY),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermitDialogs(state: PermitUiState, onDismiss: () -> Unit, onOpenSettings: () -> Unit) {
    val permit = state.permit
    when (state.dialog) {
        PermitDialog.ZONE_INFO -> InfoDialog(
            Icons.Default.Info, stringResource(R.string.permit_zone_label, state.zone.code),
            stringResource(state.zone.rulesRes), onDismiss
        )
        PermitDialog.PERMIT_DETAILS -> if (permit != null) InfoDialog(
            Icons.Default.DirectionsCar, stringResource(R.string.permit_your_permit),
            stringResource(R.string.permit_details_body, permit.zoneCode, permit.reference, permit.vehicleRegistration, formatDate(permit)),
            onDismiss
        )
        PermitDialog.PERMIT_EXPIRED -> InfoDialog(
            Icons.Default.EventBusy, stringResource(R.string.permit_expired_title),
            stringResource(R.string.permit_expired_body), onDismiss
        )
        PermitDialog.NOTIFICATIONS_BLOCKED -> AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.permit_notifications_blocked_title)) },
            text = { Text(stringResource(R.string.permit_notifications_blocked_body)) },
            confirmButton = {
                TextButton(onClick = { onDismiss(); onOpenSettings() }) { Text(stringResource(R.string.permit_open_settings)) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.permit_not_now)) } }
        )
        null -> Unit
    }
}

@Composable
private fun InfoDialog(icon: ImageVector, title: String, body: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.parking_understood)) } }
    )
}

private fun formatDate(permit: ResidentPermit): String =
    permit.expiryDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
