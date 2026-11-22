package com.example.mobileapplication.ui.housing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mobileapplication.MainActivity
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.UpcomingRentPayment
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Schedules or cancels the rent reminder. An interface so the ViewModel can be tested with a fake. */
interface RentReminderScheduler {
    /** Schedules the reminder for [payment]. Returns false if it is too late (the payment is already due). */
    fun schedule(payment: UpcomingRentPayment): Boolean

    /** Cancels any scheduled rent reminder. */
    fun cancel()
}

/**
 * Schedules the rent reminder with WorkManager, so it still fires after the app
 * is closed or the phone restarts. Everything stays on the device.
 */
class WorkManagerRentReminderScheduler(
    context: Context,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : RentReminderScheduler {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun schedule(payment: UpcomingRentPayment): Boolean {
        val delay = RentReminderTiming.delayUntilReminder(now(), payment.dueDate) ?: return false
        enqueue(WORK_NAME, payment, delay)
        return true
    }

    override fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    /** Queues the reminder worker under the unique [name], replacing any earlier one with that name. */
    internal fun enqueue(name: String, payment: UpcomingRentPayment, delay: Duration) {
        val request = OneTimeWorkRequestBuilder<RentReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    KEY_DUE_DATE to payment.dueDate.toString(),
                    KEY_AMOUNT_PENCE to payment.amountPence
                )
            )
            .build()
        workManager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }

    internal companion object {
        const val WORK_NAME = "rent_due_reminder"
        const val KEY_DUE_DATE = "due_date"
        const val KEY_AMOUNT_PENCE = "amount_pence"
    }
}

/** Shows the local "rent is due soon" notification. No server or push service is involved. */
class RentReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        // Permission may have been withdrawn since the reminder was set; then there is nothing to show.
        if (!canPostNotifications(context)) return Result.success()
        val dueDate = inputData.getString(WorkManagerRentReminderScheduler.KEY_DUE_DATE)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return Result.success()
        val amountPence = inputData.getLong(WorkManagerRentReminderScheduler.KEY_AMOUNT_PENCE, -1L)
        if (amountPence < 0) return Result.success()

        createChannel(context)
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val text = context.getString(
            R.string.rent_notification_text, formatPence(amountPence), formatLongDate(dueDate)
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_rent)
            .setContentTitle(context.getString(R.string.rent_notification_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission was removed between the check and notify(); nothing to show.
        }
        return Result.success()
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.rent_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.rent_notification_channel_description) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "rent_reminders"
        const val NOTIFICATION_ID = 5101
    }
}
