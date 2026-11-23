package com.example.domain

import com.example.data.model.BillingFrequency
import com.example.data.model.SourceTag
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = false)
data class BackupItemDto(
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
    val notes: String
)

@JsonClass(generateAdapter = false)
data class SubJoggerBackupPayload(
    val schemaVersion: Int = 1,
    val exportedAtEpochMs: Long = System.currentTimeMillis(),
    val currencyCode: String = "USD",
    val alertLeadDays: Int = 3,
    val items: List<BackupItemDto>
)

object JsonBackupManager {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(SubJoggerBackupPayload::class.java).indent("  ")

    fun exportToJson(
        items: List<SubscriptionItem>,
        currencyCode: String,
        alertLeadDays: Int
    ): String {
        val payload = SubJoggerBackupPayload(
            schemaVersion = 1,
            exportedAtEpochMs = System.currentTimeMillis(),
            currencyCode = currencyCode,
            alertLeadDays = alertLeadDays,
            items = items.map { item ->
                BackupItemDto(
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
                    notes = item.notes
                )
            }
        )
        return adapter.toJson(payload)
    }

    fun parseBackupJson(json: String): Result<Pair<SubJoggerBackupPayload, List<SubscriptionItem>>> {
        return runCatching {
            val payload = adapter.fromJson(json)
                ?: throw IllegalArgumentException("Invalid SubJogger backup file.")
            require(payload.schemaVersion >= 1) { "Unsupported backup schema version." }
            val domainItems = payload.items.map { dto ->
                SubscriptionItem(
                    name = dto.name.ifBlank { "Untitled Subscription" },
                    amount = dto.amount,
                    currencyCode = dto.currencyCode.ifBlank { payload.currencyCode },
                    frequency = BillingFrequency.entries.find { it.name == dto.frequency } ?: BillingFrequency.MONTHLY,
                    category = SubscriptionCategory.entries.find { it.name == dto.category } ?: SubscriptionCategory.OTHER,
                    nextBillingEpochDay = dto.nextBillingEpochDay,
                    isTrial = dto.isTrial,
                    isCancelled = dto.isCancelled,
                    needsReview = dto.needsReview,
                    confidencePercent = dto.confidencePercent,
                    sourceTag = SourceTag.entries.find { it.name == dto.sourceTag } ?: SourceTag.MANUAL,
                    paymentHint = dto.paymentHint,
                    notes = dto.notes
                )
            }
            payload to domainItems
        }
    }
}
