package com.example.mobileapplication.ui.parking.permit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

/**
 * Returns an action that makes sure notifications are allowed before a reminder is set.
 * On Android 13+ it shows the system prompt the first time; it never asks before the user turns the switch on.
 */
@Composable
fun rememberNotificationPermissionRequest(onAllowed: () -> Unit, onBlocked: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val latestOnAllowed by rememberUpdatedState(onAllowed)
    val latestOnBlocked by rememberUpdatedState(onBlocked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && canPostNotifications(context)) latestOnAllowed() else latestOnBlocked()
    }
    return remember(context, launcher) {
        {
            when {
                canPostNotifications(context) -> latestOnAllowed()
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else -> latestOnBlocked() // Android 12 and lower: notifications were turned off in Settings.
            }
        }
    }
}

/** Opens this app's notification settings so the user can allow reminders later. */
fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
