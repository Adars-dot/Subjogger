package com.example.data.local

import com.example.data.model.BillingFrequency
import com.example.data.model.JoggerCategoryGroup
import com.example.data.model.JoggerTemplate
import com.example.data.model.SourceTag
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem
import com.example.domain.DateUtils

object SampleDataProvider {

    fun createSampleLedger(currencyCode: String = "USD"): List<SubscriptionItem> {
        val today = DateUtils.todayEpochDay()
        return listOf(
            SubscriptionItem(
                name = "Netflix Standard",
                amount = 15.49,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.VIDEO,
                nextBillingEpochDay = today + 3,
                sourceTag = SourceTag.SAMPLE,
                paymentHint = "•••• 4821",
                notes = "Streaming video plan"
            ),
            SubscriptionItem(
                name = "Spotify Duo",
                amount = 10.99,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.MUSIC,
                nextBillingEpochDay = today + 6,
                sourceTag = SourceTag.SAMPLE,
                paymentHint = "•••• 4821",
                notes = "Music & podcasts"
            ),
            SubscriptionItem(
                name = "Electricity Bill",
                amount = 65.00,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.BILLS,
                nextBillingEpochDay = today + 12,
                sourceTag = SourceTag.SAMPLE,
                notes = "Monthly home power utility"
            ),
            SubscriptionItem(
                name = "ChatGPT Plus",
                amount = 20.00,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.SOFTWARE,
                nextBillingEpochDay = today + 19,
                sourceTag = SourceTag.SAMPLE,
                notes = "AI productivity plan"
            ),
            SubscriptionItem(
                name = "Notion Plus",
                amount = null,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.SOFTWARE,
                nextBillingEpochDay = today + 24,
                sourceTag = SourceTag.SAMPLE,
                notes = "Tap + Add amount to log exact monthly charge"
            ),
            SubscriptionItem(
                name = "Fitness First",
                amount = 45.00,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.FITNESS,
                nextBillingEpochDay = today - 10,
                isCancelled = true,
                sourceTag = SourceTag.SAMPLE,
                notes = "Cancelled membership"
            ),
            SubscriptionItem(
                name = "Adobe Creative Cloud",
                amount = 54.99,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.SOFTWARE,
                nextBillingEpochDay = today + 8,
                isTrial = true,
                needsReview = true,
                confidencePercent = 65,
                sourceTag = SourceTag.SAMPLE,
                notes = "Trial renewal estimate — confirm pricing"
            ),
            SubscriptionItem(
                name = "iCloud+ 2TB",
                amount = 9.99,
                currencyCode = currencyCode,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.CLOUD,
                nextBillingEpochDay = today + 15,
                needsReview = true,
                confidencePercent = 78,
                sourceTag = SourceTag.SAMPLE,
                notes = "Verify Apple ID billing cycle"
            )
        )
    }

    val sampleReceiptText: String = """
        Thanks for renewing Figma Professional!
        Order #INV-99812402 | Account: sarah.chen@figma-user.org
        Card charged: Visa ending in 4821 (4532 8810 2231 4821)
        Support phone: (415) 555-0199
        Amount: $15.00 / month
        Next Charge Date: in 14 days

        Also upcoming: Adobe Creative Cloud
        Estimated Trial Renewal: $54.99 / mo
        Trial ends in 6 days
    """.trimIndent()

    val joggerCategories: List<JoggerCategoryGroup> = listOf(
        JoggerCategoryGroup(
            id = "streaming",
            title = "Streaming & Video",
            promptHint = "Check smart TV apps, Apple TV channels, and family bundles.",
            templates = listOf(
                JoggerTemplate("netflix", "Netflix", 15.49, BillingFrequency.MONTHLY, SubscriptionCategory.VIDEO, "Standard plan"),
                JoggerTemplate("youtube", "YouTube Premium", 13.99, BillingFrequency.MONTHLY, SubscriptionCategory.VIDEO, "Ad-free video & music"),
                JoggerTemplate("disney", "Disney+", 13.99, BillingFrequency.MONTHLY, SubscriptionCategory.VIDEO, "Streaming bundle"),
                JoggerTemplate("prime", "Amazon Prime", 139.00, BillingFrequency.YEARLY, SubscriptionCategory.VIDEO, "Annual Prime renewal")
            )
        ),
        JoggerCategoryGroup(
            id = "music_cloud",
            title = "Music & Cloud Storage",
            promptHint = "Silent monthly backups often hide in App Store or Google Play billing.",
            templates = listOf(
                JoggerTemplate("spotify", "Spotify", 11.99, BillingFrequency.MONTHLY, SubscriptionCategory.MUSIC, "Individual/Duo"),
                JoggerTemplate("google_one", "Google One 2TB", 9.99, BillingFrequency.MONTHLY, SubscriptionCategory.CLOUD, "Photos & Drive backup"),
                JoggerTemplate("icloud", "iCloud+ 200GB", 2.99, BillingFrequency.MONTHLY, SubscriptionCategory.CLOUD, "Apple cloud storage"),
                JoggerTemplate("dropbox", "Dropbox Plus", 119.88, BillingFrequency.YEARLY, SubscriptionCategory.CLOUD, "Annual cloud sync")
            )
        ),
        JoggerCategoryGroup(
            id = "ai_software",
            title = "AI & Productivity Tools",
            promptHint = "Think about developer seats, design subscriptions, and password managers.",
            templates = listOf(
                JoggerTemplate("chatgpt", "ChatGPT Plus", 20.00, BillingFrequency.MONTHLY, SubscriptionCategory.SOFTWARE, "AI assistant"),
                JoggerTemplate("figma", "Figma Pro", 15.00, BillingFrequency.MONTHLY, SubscriptionCategory.SOFTWARE, "Design workspace"),
                JoggerTemplate("1password", "1Password", 35.88, BillingFrequency.YEARLY, SubscriptionCategory.SOFTWARE, "Annual security vault"),
                JoggerTemplate("github", "GitHub Copilot", 10.00, BillingFrequency.MONTHLY, SubscriptionCategory.SOFTWARE, "Coding assistant")
            )
        ),
        JoggerCategoryGroup(
            id = "bills_fitness",
            title = "Utilities, Domains & Fitness",
            promptHint = "Don't forget annual domain registrars, internet bills, or gym memberships.",
            templates = listOf(
                JoggerTemplate("internet", "Fiber Internet", 70.00, BillingFrequency.MONTHLY, SubscriptionCategory.BILLS, "Home broadband"),
                JoggerTemplate("mobile", "Mobile Phone Plan", 45.00, BillingFrequency.MONTHLY, SubscriptionCategory.BILLS, "Wireless carrier"),
                JoggerTemplate("domain", "Domain Registrar", 14.00, BillingFrequency.YEARLY, SubscriptionCategory.BILLS, "Annual .com renewal"),
                JoggerTemplate("gym", "Local Gym Pass", 39.00, BillingFrequency.MONTHLY, SubscriptionCategory.FITNESS, "Monthly club dues")
            )
        )
    )
}
