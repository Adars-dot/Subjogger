package com.example.data.model

import java.util.Locale

enum class BillingFrequency(val label: String, val shortLabel: String, val rruleFreq: String) {
    MONTHLY("Monthly", "mo", "MONTHLY"),
    YEARLY("Yearly", "yr", "YEARLY"),
    WEEKLY("Weekly", "wk", "WEEKLY")
}

enum class SubscriptionCategory(val label: String, val isBill: Boolean) {
    VIDEO("Video", false),
    MUSIC("Music", false),
    SOFTWARE("Software", false),
    CLOUD("Cloud", false),
    FITNESS("Fitness", false),
    BILLS("Bills", true),
    OTHER("Other", false)
}

enum class SourceTag(val label: String) {
    MANUAL("Manual"),
    TEXT_SCRUB("Paste & Scrub"),
    SCREENSHOT_OCR("Screenshot OCR"),
    JOGGER("Memory Jogger"),
    SAMPLE("Sample")
}

enum class CurrencyOption(val code: String, val symbol: String, val displayName: String) {
    USD("USD", "$", "USD ($)"),
    EUR("EUR", "€", "EUR (€)"),
    GBP("GBP", "£", "GBP (£)"),
    CAD("CAD", "CA$", "CAD ($)"),
    AUD("AUD", "A$", "AUD ($)"),
    JPY("JPY", "¥", "JPY (¥)");

    companion object {
        fun fromCode(code: String): CurrencyOption =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: USD
    }
}

fun formatMoney(amount: Double?, currencyCode: String): String {
    val currency = CurrencyOption.fromCode(currencyCode)
    if (amount == null) return "—"
    return if (currency == CurrencyOption.JPY) {
        "${currency.symbol}${String.format(Locale.US, "%,.0f", amount)}"
    } else {
        "${currency.symbol}${String.format(Locale.US, "%,.2f", amount)}"
    }
}

data class SubscriptionItem(
    val id: Long = 0,
    val name: String,
    val amount: Double?,
    val currencyCode: String = "USD",
    val frequency: BillingFrequency = BillingFrequency.MONTHLY,
    val category: SubscriptionCategory = SubscriptionCategory.SOFTWARE,
    val nextBillingEpochDay: Long,
    val isTrial: Boolean = false,
    val isCancelled: Boolean = false,
    val needsReview: Boolean = false,
    val confidencePercent: Int = 100,
    val sourceTag: SourceTag = SourceTag.MANUAL,
    val paymentHint: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ParsedCandidate(
    val tempId: String,
    val name: String,
    val amount: Double?,
    val currencyCode: String = "USD",
    val frequency: BillingFrequency = BillingFrequency.MONTHLY,
    val category: SubscriptionCategory = SubscriptionCategory.SOFTWARE,
    val nextBillingEpochDay: Long,
    val isTrial: Boolean = false,
    val confidencePercent: Int = 92,
    val paymentHint: String = "",
    val selected: Boolean = true,
    val sourceTag: SourceTag = SourceTag.TEXT_SCRUB
)

data class JoggerTemplate(
    val id: String,
    val name: String,
    val suggestedAmount: Double?,
    val frequency: BillingFrequency,
    val category: SubscriptionCategory,
    val note: String
)

data class JoggerCategoryGroup(
    val id: String,
    val title: String,
    val promptHint: String,
    val templates: List<JoggerTemplate>
)
