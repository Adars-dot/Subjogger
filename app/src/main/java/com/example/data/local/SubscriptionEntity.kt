package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.BillingFrequency
import com.example.data.model.SourceTag
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double?,
    val currencyCode: String,
    val frequency: String,
    val category: String,
    val nextBillingEpochDay: Long,
    val isTrial: Boolean,
    val isCancelled: Boolean,
    val needsReview: Boolean,
    val confidencePercent: Int,
    val sourceTag: String,
    val paymentHint: String,
    val notes: String,
    val updatedAt: Long
) {
    fun toDomain(): SubscriptionItem = SubscriptionItem(
        id = id,
        name = name,
        amount = amount,
        currencyCode = currencyCode,
        frequency = BillingFrequency.entries.find { it.name == frequency } ?: BillingFrequency.MONTHLY,
        category = SubscriptionCategory.entries.find { it.name == category } ?: SubscriptionCategory.OTHER,
        nextBillingEpochDay = nextBillingEpochDay,
        isTrial = isTrial,
        isCancelled = isCancelled,
        needsReview = needsReview,
        confidencePercent = confidencePercent,
        sourceTag = SourceTag.entries.find { it.name == sourceTag } ?: SourceTag.MANUAL,
        paymentHint = paymentHint,
        notes = notes,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(item: SubscriptionItem): SubscriptionEntity = SubscriptionEntity(
            id = item.id,
            name = item.name,
            amount = item.amount,
            currencyCode = item.currencyCode,
            frequency = item.frequency.name,
            category = item.category.name,
            nextBillingEpochDay = item.nextBillingEpochDay,
            isTrial = item.isTrial,
            isCancelled = item.isCancelled,
            needsReview = item.needsReview,
            confidencePercent = item.confidencePercent,
            sourceTag = item.sourceTag.name,
            paymentHint = item.paymentHint,
            notes = item.notes,
            updatedAt = item.updatedAt
        )
    }
}
