package com.example.mobileapplication.ui.parking.permit

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mobileapplication.MainActivity
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.ResidentPermit
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit

/** When the expiry reminder fires. Free of Android types so it can be unit tested. */
object ReminderTiming {
    const val DAYS_BEFORE_EXPIRY = 14L
    private const val REMINDER_HOUR = 9

    fun reminderTime(expiry: LocalDate): LocalDateTime =
        expiry.minusDays(DAYS_BEFORE_EXPIRY).atTime(REMINDER_HOUR, 0)

    /**
     * How long to wait before reminding. Fires straight away if the reminder time has already passed
     * but the permit is still valid; returns null once the permit has expired.
     */
    fun delayUntilReminder(now: LocalDateTime, expiry: LocalDate): Duration? {
        if (!now.toLocalDate().isBefore(expiry.plusDays(1))) return null
        val delay = Duration.between(now, reminderTime(expiry))
        return if (delay.isNegative) Duration.ZERO else delay
    }
}

/** Schedules or cancels the expiry reminder. An interface so the ViewModel can be tested with a fake. */
interface PermitReminderScheduler {
    /** Returns false if no reminder can be set because the permit has already expired. */
    fun schedule(permit: ResidentPermit): Boolean
    fun cancel()
}

/** Uses WorkManager, so the reminder survives the app closing and the phone restarting. */
class WorkManagerPermitReminderScheduler(
    context: Context,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : PermitReminderScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun schedule(permit: ResidentPermit): Boolean {
        val delay = ReminderTiming.delayUntilReminder(now(), permit.expiryDate) ?: return false
        val request = OneTimeWorkRequestBuilder<PermitReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_ZONE to permit.zoneCode, KEY_EXPIRY to permit.expiryDate.toString()))
            .addTag(WORK_NAME)
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        return true
    }

    override fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    internal companion object {
        const val WORK_NAME = "resident_permit_expiry_reminder"
        const val KEY_ZONE = "zone"
        const val KEY_EXPIRY = "expiry"
    }
}

/** Shows the local "permit expiring soon" notification. No server or push service is involved. */
class PermitReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        // Permission may have been withdrawn since the reminder was set; then there is nothing to show.
        if (!canPostNotifications(context)) return Result.success()
        val zone = inputData.getString(WorkManagerPermitReminderScheduler.KEY_ZONE) ?: return Result.success()
        val expiry = inputData.getString(WorkManagerPermitReminderScheduler.KEY_EXPIRY)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return Result.success()

        createChannel(context)
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val expiryText = expiry.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_permit)
            .setContentTitle(context.getString(R.string.permit_notification_title))
            .setContentText(context.getString(R.string.permit_notification_text, zone, expiryText))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.permit_notification_text, zone, expiryText)))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission removed between the check and notify().
        }
        return Result.success()
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(R.string.permit_notification_channel), NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.permit_notification_channel_description) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "permit_reminders"
        const val NOTIFICATION_ID = 4101
    }
}

/** True when this app may show notifications (runtime permission on Android 13+, and not blocked in Settings). */
fun canPostNotifications(context: Context): Boolean {
    val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
}
