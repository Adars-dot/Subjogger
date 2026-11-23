package com.example.domain

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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.local.SubJoggerDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.SubscriptionItem
import com.example.data.model.formatMoney

class RenewalNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesRepository(applicationContext).getSnapshot()
        if (!prefs.notificationsEnabled) return Result.success()

        val items = SubJoggerDatabase.getInstance(applicationContext)
            .subscriptionDao()
            .getAllOnce()
            .map { it.toDomain() }

        val dueSoon = findUpcomingRenewals(items, prefs.alertLeadDays)
        if (dueSoon.isNotEmpty()) {
            postRenewalNotification(applicationContext, dueSoon, prefs.alertLeadDays)
        }
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "subjogger_renewals"
        private const val NOTIFICATION_ID = 4102

        fun findUpcomingRenewals(items: List<SubscriptionItem>, leadDays: Int): List<SubscriptionItem> {
            val today = DateUtils.todayEpochDay()
            return items.filter { item ->
                !item.isCancelled &&
                    (item.nextBillingEpochDay - today) in 0L..leadDays.toLong()
            }.sortedBy { it.nextBillingEpochDay }
        }

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = context.getString(R.string.notification_channel_desc)
                    }
                    manager.createNotificationChannel(channel)
                }
            }
        }

        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        fun postRenewalNotification(
            context: Context,
            dueItems: List<SubscriptionItem>,
            leadDays: Int,
            isPreview: Boolean = false
        ): Boolean {
            if (!hasNotificationPermission(context)) return false
            ensureChannel(context)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = when {
                dueItems.isEmpty() -> "SubJogger Local Alerts Active"
                dueItems.size == 1 -> {
                    val sub = dueItems.first()
                    val amt = if (sub.amount != null) " (${formatMoney(sub.amount, sub.currencyCode)})" else ""
                    "Upcoming renewal: ${sub.name}$amt"
                }
                else -> "${dueItems.size} renewals within $leadDays days"
            }

            val body = if (dueItems.isEmpty()) {
                "No charges due in the next $leadDays days. 100% on-device alerts are monitoring your ledger."
            } else {
                dueItems.take(3).joinToString(" • ") { sub ->
                    val diff = DateUtils.daysUntil(sub.nextBillingEpochDay)
                    val whenStr = when {
                        diff <= 0L -> "today"
                        diff == 1L -> "tomorrow"
                        else -> "in ${diff}d"
                    }
                    "${sub.name} ($whenStr)"
                }
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(if (isPreview) "$title [Preview]" else title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            return true
        }
    }
}
