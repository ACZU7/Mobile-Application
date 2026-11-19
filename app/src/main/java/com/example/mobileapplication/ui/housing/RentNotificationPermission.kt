package com.example.mobileapplication.ui.housing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

// The Parking permit reminder (feature/parking-directions-location, PR #5) has the same
// helpers in ui/parking/permit. Once that PR is merged, these can be combined into one shared file.

/** True when this app may show notifications (runtime permission on Android 13+, and not blocked in Settings). */
internal fun canPostNotifications(context: Context): Boolean {
    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
}

/**
 * Returns an action that makes sure notifications are allowed before a reminder is set.
 *
 * Nothing is asked when the screen opens: the system prompt is only shown when the
 * returned action runs, which is when the user turns the reminder switch on.
 * [onAllowed] runs if notifications can be shown; [onBlocked] runs if the user
 * refuses, or notifications are turned off in Settings.
 */
@Composable
internal fun rememberNotificationPermissionRequest(onAllowed: () -> Unit, onBlocked: () -> Unit): () -> Unit {
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
internal fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
