package com.example.domain

import com.example.data.model.BillingFrequency
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem

data class CategoryBreakdown(
    val category: SubscriptionCategory,
    val monthlyAmount: Double,
    val itemCount: Int,
    val sharePercent: Float
)

data class LedgerSummary(
    val monthlyRunRate: Double,
    val yearlyEquivalent: Double,
    val next30DaysCount: Int,
    val next30DaysTotal: Double,
    val activeCount: Int,
    val subscriptionsOnlyCount: Int,
    val billsOnlyCount: Int,
    val reviewQueueCount: Int,
    val trialCount: Int,
    val cancelledCount: Int,
    val categoryBreakdowns: List<CategoryBreakdown>
)

object Calculations {

    fun monthlyEquivalent(item: SubscriptionItem): Double {
        val amt = item.amount ?: return 0.0
        return when (item.frequency) {
            BillingFrequency.MONTHLY -> amt
            BillingFrequency.YEARLY -> amt / 12.0
            BillingFrequency.WEEKLY -> amt * 52.0 / 12.0
        }
    }

    fun summarize(items: List<SubscriptionItem>): LedgerSummary {
        val activeConfirmed = items.filter { !it.isCancelled && !it.needsReview }
        val monthlyRunRate = activeConfirmed.sumOf { monthlyEquivalent(it) }
        val yearlyEquivalent = monthlyRunRate * 12.0

        val today = DateUtils.todayEpochDay()
        val next30Items = activeConfirmed.filter {
            val diff = it.nextBillingEpochDay - today
            diff in 0L..30L && it.amount != null
        }

        val breakdownMap = activeConfirmed
            .filter { it.amount != null }
            .groupBy { it.category }

        val breakdowns = breakdownMap.map { (category, group) ->
            val sum = group.sumOf { monthlyEquivalent(it) }
            val share = if (monthlyRunRate > 0.0) (sum / monthlyRunRate).toFloat() else 0f
            CategoryBreakdown(
                category = category,
                monthlyAmount = sum,
                itemCount = group.size,
                sharePercent = share
            )
        }.sortedByDescending { it.monthlyAmount }

        return LedgerSummary(
            monthlyRunRate = monthlyRunRate,
            yearlyEquivalent = yearlyEquivalent,
            next30DaysCount = next30Items.size,
            next30DaysTotal = next30Items.sumOf { it.amount ?: 0.0 },
            activeCount = activeConfirmed.size,
            subscriptionsOnlyCount = items.count { !it.category.isBill && !it.needsReview },
            billsOnlyCount = items.count { it.category.isBill && !it.needsReview },
            reviewQueueCount = items.count { it.needsReview },
            trialCount = items.count { it.isTrial && !it.isCancelled },
            cancelledCount = items.count { it.isCancelled },
            categoryBreakdowns = breakdowns
        )
    }
}
