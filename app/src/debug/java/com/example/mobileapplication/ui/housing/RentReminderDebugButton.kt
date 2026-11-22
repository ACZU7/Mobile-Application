package com.example.mobileapplication.ui.housing

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.UpcomingRentPayment
import java.time.Duration

/**
 * DEBUG BUILDS ONLY: a button that shows the rent reminder notification straight
 * away, so it can be tested without waiting until 3 days before the due date.
 *
 * The release source set has an empty version of this function, so the button
 * and its code never exist in a release build.
 */
@Composable
fun RentReminderDebugButton(
    payment: UpcomingRentPayment,
    onNotificationsBlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheduler = remember(context) { WorkManagerRentReminderScheduler(context) }
    val showNow = rememberNotificationPermissionRequest(
        onAllowed = { scheduler.showTestReminderNow(payment) },
        onBlocked = onNotificationsBlocked
    )
    OutlinedButton(onClick = showNow, modifier = modifier) {
        Text(stringResource(R.string.rent_debug_test_reminder))
    }
}

/** Shows the reminder for [payment] straight away (debug builds only). */
private fun WorkManagerRentReminderScheduler.showTestReminderNow(payment: UpcomingRentPayment) {
    enqueue("rent_due_reminder_test", payment, Duration.ZERO)
}
