package com.example.domain

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.model.SubscriptionItem
import java.util.concurrent.TimeUnit

object AlertScheduler {
    private const val WORK_NAME = "subjogger_daily_renewal_check"

    fun syncSchedule(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            RenewalNotificationWorker.ensureChannel(context)
            val request = PeriodicWorkRequestBuilder<RenewalNotificationWorker>(
                12,
                TimeUnit.HOURS
            ).build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        } else {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }

    fun sendTestNotification(
        context: Context,
        items: List<SubscriptionItem>,
        leadDays: Int
    ): Boolean {
        val dueSoon = RenewalNotificationWorker.findUpcomingRenewals(items, leadDays)
        return RenewalNotificationWorker.postRenewalNotification(
            context = context,
            dueItems = dueSoon,
            leadDays = leadDays,
            isPreview = true
        )
    }
}
