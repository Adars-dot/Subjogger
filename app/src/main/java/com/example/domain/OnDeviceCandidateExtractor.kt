package com.example.domain

import com.example.data.model.BillingFrequency
import com.example.data.model.ParsedCandidate
import com.example.data.model.SourceTag
import com.example.data.model.SubscriptionCategory
import java.util.UUID

object OnDeviceCandidateExtractor {

    private data class KnownMerchant(
        val keyword: String,
        val displayName: String,
        val category: SubscriptionCategory,
        val defaultAmount: Double? = null,
        val defaultFrequency: BillingFrequency = BillingFrequency.MONTHLY
    )

    private val knownMerchants = listOf(
        KnownMerchant("figma", "Figma Professional", SubscriptionCategory.SOFTWARE, 15.00),
        KnownMerchant("adobe", "Adobe Creative Cloud", SubscriptionCategory.SOFTWARE, 54.99),
        KnownMerchant("netflix", "Netflix", SubscriptionCategory.VIDEO, 15.49),
        KnownMerchant("spotify", "Spotify", SubscriptionCategory.MUSIC, 11.99),
        KnownMerchant("chatgpt", "ChatGPT Plus", SubscriptionCategory.SOFTWARE, 20.00),
        KnownMerchant("openai", "ChatGPT Plus", SubscriptionCategory.SOFTWARE, 20.00),
        KnownMerchant("notion", "Notion", SubscriptionCategory.SOFTWARE, 10.00),
        KnownMerchant("icloud", "iCloud+", SubscriptionCategory.CLOUD, 2.99),
        KnownMerchant("google one", "Google One", SubscriptionCategory.CLOUD, 9.99),
        KnownMerchant("youtube", "YouTube Premium", SubscriptionCategory.VIDEO, 13.99),
        KnownMerchant("disney", "Disney+", SubscriptionCategory.VIDEO, 13.99),
        KnownMerchant("amazon prime", "Amazon Prime", SubscriptionCategory.VIDEO, 139.00, BillingFrequency.YEARLY),
        KnownMerchant("midjourney", "Midjourney", SubscriptionCategory.SOFTWARE, 30.00),
        KnownMerchant("github", "GitHub Copilot", SubscriptionCategory.SOFTWARE, 10.00),
        KnownMerchant("dropbox", "Dropbox", SubscriptionCategory.CLOUD, 11.99),
        KnownMerchant("1password", "1Password", SubscriptionCategory.SOFTWARE, 35.88, BillingFrequency.YEARLY),
        KnownMerchant("electric", "Electricity Bill", SubscriptionCategory.BILLS, 65.00),
        KnownMerchant("internet", "Internet Bill", SubscriptionCategory.BILLS, 70.00),
        KnownMerchant("verizon", "Verizon Wireless", SubscriptionCategory.BILLS, 65.00),
        KnownMerchant("at&t", "AT&T Wireless", SubscriptionCategory.BILLS, 65.00),
        KnownMerchant("fitness", "Fitness Membership", SubscriptionCategory.FITNESS, 45.00),
        KnownMerchant("gym", "Gym Membership", SubscriptionCategory.FITNESS, 39.00)
    )

    private val priceRegex = Regex("""([$€£¥])\s?(\d{1,5}(?:[.,]\d{2})?)""")
    private val inDaysRegex = Regex("""\bin\s+(\d{1,3})\s+days?\b""", RegexOption.IGNORE_CASE)
    private val yearlyCueRegex = Regex("""\b(year|yearly|annual|annually|/yr)\b""", RegexOption.IGNORE_CASE)
    private val weeklyCueRegex = Regex("""\b(week|weekly|/wk)\b""", RegexOption.IGNORE_CASE)
    private val trialCueRegex = Regex("""\b(trial|free\s+for|intro|estimated)\b""", RegexOption.IGNORE_CASE)

    fun extractCandidates(
        scrubbedText: String,
        paymentHint: String,
        defaultCurrency: String,
        sourceTag: SourceTag
    ): List<ParsedCandidate> {
        if (scrubbedText.isBlank()) return emptyList()

        val today = DateUtils.todayEpochDay()
        // Split into logical blocks on blank lines or double line breaks, or scan line windows
        val blocks = scrubbedText
            .split(Regex("""\n\s*\n"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val candidates = mutableListOf<ParsedCandidate>()
        val seenNames = mutableSetOf<String>()

        for (block in blocks) {
            val lower = block.lowercase()
            val matchedMerchants = knownMerchants.filter { lower.contains(it.keyword) }
            if (matchedMerchants.isNotEmpty()) {
                for (merchant in matchedMerchants) {
                    if (seenNames.add(merchant.displayName.lowercase())) {
                        val extractedPrice = extractPriceFromWindow(block, merchant.keyword)
                        val amount = extractedPrice?.first ?: merchant.defaultAmount
                        val currency = extractedPrice?.second ?: defaultCurrency
                        val freq = detectFrequency(block, merchant.defaultFrequency)
                        val daysAhead = detectDaysAhead(block) ?: 14L
                        val isTrial = trialCueRegex.containsMatchIn(block)
                        val confidence = when {
                            extractedPrice != null && !isTrial -> 96
                            extractedPrice != null && isTrial -> 68
                            else -> 64
                        }
                        candidates += ParsedCandidate(
                            tempId = UUID.randomUUID().toString(),
                            name = merchant.displayName,
                            amount = amount,
                            currencyCode = currency,
                            frequency = freq,
                            category = merchant.category,
                            nextBillingEpochDay = today + daysAhead,
                            isTrial = isTrial,
                            confidencePercent = confidence,
                            paymentHint = paymentHint,
                            selected = true,
                            sourceTag = sourceTag
                        )
                    }
                }
            } else {
                // Fallback generic extraction from lines containing a currency price
                val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
                for (line in lines) {
                    val priceMatch = priceRegex.find(line) ?: continue
                    val rawAmt = priceMatch.groupValues[2].replace(",", ".").toDoubleOrNull() ?: continue
                    val currency = symbolToCurrency(priceMatch.groupValues[1], defaultCurrency)
                    val cleanedName = line
                        .replace(priceRegex, "")
                        .replace(Regex("""\b(per\s+month|per\s+year|/mo|/month|/yr|amount|total|charge|bill|estimated|renewal)\b""", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("""[:\-|/]+"""), " ")
                        .trim()
                        .take(36)
                        .ifBlank { "Detected Subscription" }

                    if (seenNames.add(cleanedName.lowercase())) {
                        val isTrial = trialCueRegex.containsMatchIn(block)
                        candidates += ParsedCandidate(
                            tempId = UUID.randomUUID().toString(),
                            name = cleanedName,
                            amount = rawAmt,
                            currencyCode = currency,
                            frequency = detectFrequency(line, BillingFrequency.MONTHLY),
                            category = guessCategory(cleanedName),
                            nextBillingEpochDay = today + (detectDaysAhead(block) ?: 14L),
                            isTrial = isTrial,
                            confidencePercent = if (isTrial) 65 else 82,
                            paymentHint = paymentHint,
                            selected = true,
                            sourceTag = sourceTag
                        )
                    }
                }
            }
        }

        // If text had no price or known merchant at all, surface one editable low-confidence candidate
        if (candidates.isEmpty()) {
            val firstCleanLine = scrubbedText.lines()
                .firstOrNull { it.trim().length >= 3 }
                ?.trim()
                ?.take(32)
                ?: "New Subscription"
            val priceMatch = priceRegex.find(scrubbedText)
            val amt = priceMatch?.groupValues?.get(2)?.replace(",", ".")?.toDoubleOrNull()
            candidates += ParsedCandidate(
                tempId = UUID.randomUUID().toString(),
                name = firstCleanLine,
                amount = amt,
                currencyCode = defaultCurrency,
                frequency = detectFrequency(scrubbedText, BillingFrequency.MONTHLY),
                category = guessCategory(firstCleanLine),
                nextBillingEpochDay = today + (detectDaysAhead(scrubbedText) ?: 14L),
                isTrial = trialCueRegex.containsMatchIn(scrubbedText),
                confidencePercent = 58,
                paymentHint = paymentHint,
                selected = true,
                sourceTag = sourceTag
            )
        }

        return candidates
    }

    private fun extractPriceFromWindow(block: String, keyword: String): Pair<Double, String>? {
        val lines = block.lines()
        val idx = lines.indexOfFirst { it.lowercase().contains(keyword) }
        val windowText = if (idx >= 0) {
            lines.subList(idx, minOf(lines.size, idx + 5)).joinToString("\n")
        } else {
            block
        }
        val match = priceRegex.find(windowText) ?: priceRegex.find(block) ?: return null
        val amount = match.groupValues[2].replace(",", ".").toDoubleOrNull() ?: return null
        return amount to symbolToCurrency(match.groupValues[1], "USD")
    }

    private fun symbolToCurrency(symbol: String, fallback: String): String = when (symbol) {
        "€" -> "EUR"
        "£" -> "GBP"
        "¥" -> "JPY"
        "$" -> if (fallback in listOf("USD", "CAD", "AUD")) fallback else "USD"
        else -> fallback
    }

    private fun detectFrequency(text: String, defaultFreq: BillingFrequency): BillingFrequency = when {
        yearlyCueRegex.containsMatchIn(text) -> BillingFrequency.YEARLY
        weeklyCueRegex.containsMatchIn(text) -> BillingFrequency.WEEKLY
        else -> defaultFreq
    }

    private fun detectDaysAhead(text: String): Long? {
        val match = inDaysRegex.find(text) ?: return null
        return match.groupValues[1].toLongOrNull()?.coerceIn(1L, 365L)
    }

    private fun guessCategory(name: String): SubscriptionCategory {
        val lower = name.lowercase()
        return when {
            lower.contains("tv") || lower.contains("stream") || lower.contains("video") || lower.contains("movie") -> SubscriptionCategory.VIDEO
            lower.contains("music") || lower.contains("audio") || lower.contains("podcast") -> SubscriptionCategory.MUSIC
            lower.contains("cloud") || lower.contains("drive") || lower.contains("storage") -> SubscriptionCategory.CLOUD
            lower.contains("gym") || lower.contains("fit") || lower.contains("yoga") -> SubscriptionCategory.FITNESS
            lower.contains("electric") || lower.contains("water") || lower.contains("internet") || lower.contains("bill") || lower.contains("mobile") -> SubscriptionCategory.BILLS
            else -> SubscriptionCategory.SOFTWARE
        }
    }
}
