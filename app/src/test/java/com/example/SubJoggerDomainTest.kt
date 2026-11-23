package com.example

import com.example.data.model.BillingFrequency
import com.example.data.model.SourceTag
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem
import com.example.domain.Calculations
import com.example.domain.DateUtils
import com.example.domain.IcsGenerator
import com.example.domain.JsonBackupManager
import com.example.domain.OnDeviceCandidateExtractor
import com.example.domain.PrivacyScrubber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubJoggerDomainTest {

    @Test
    fun privacyScrubber_masksCardsEmailsPhonesAndOrderIds() {
        val raw = """
            Figma Pro receipt
            Card: 4532-8810-2231-4821
            Email: sarah.chen@figma.com
            Phone: (415) 555-0199
            Order #INV-99812402
        """.trimIndent()

        val result = PrivacyScrubber.scrubText(raw)
        assertTrue(result.cardMasked)
        assertTrue(result.emailObfuscated)
        assertTrue(result.phoneRedacted)
        assertTrue(result.idMasked)
        assertTrue(result.redactedCount >= 4)
        assertTrue(result.scrubbedText.contains("[•••• 4821]"))
        assertTrue(result.scrubbedText.contains("s***@figma.com"))
        assertTrue(result.scrubbedText.contains("***-***-0199"))
        assertTrue(result.scrubbedText.contains("[MASKED_ID]"))
        assertFalse(result.scrubbedText.contains("4532-8810-2231-4821"))
        assertFalse(result.scrubbedText.contains("sarah.chen@figma.com"))
    }

    @Test
    fun candidateExtractor_parsesMultiSubscriptionText() {
        val scrubbed = """
            Figma Professional
            [•••• 4821]
            Amount: $15.00 / month
            Next charge in 14 days

            Adobe Creative Cloud
            Estimated Trial Renewal: $54.99 / mo
        """.trimIndent()

        val candidates = OnDeviceCandidateExtractor.extractCandidates(
            scrubbedText = scrubbed,
            paymentHint = "•••• 4821",
            defaultCurrency = "USD",
            sourceTag = SourceTag.TEXT_SCRUB
        )

        assertEquals(2, candidates.size)
        assertEquals("Figma Professional", candidates[0].name)
        assertEquals(15.00, candidates[0].amount ?: 0.0, 0.01)
        assertEquals("Adobe Creative Cloud", candidates[1].name)
        assertTrue(candidates[1].isTrial)
    }

    @Test
    fun calculations_normalizesYearlyAndExcludesCancelledAndReview() {
        val today = DateUtils.todayEpochDay()
        val items = listOf(
            SubscriptionItem(
                id = 1,
                name = "Netflix",
                amount = 15.00,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.VIDEO,
                nextBillingEpochDay = today + 3
            ),
            SubscriptionItem(
                id = 2,
                name = "Prime",
                amount = 120.00,
                frequency = BillingFrequency.YEARLY,
                category = SubscriptionCategory.VIDEO,
                nextBillingEpochDay = today + 10
            ),
            SubscriptionItem(
                id = 3,
                name = "Cancelled Gym",
                amount = 50.00,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.FITNESS,
                nextBillingEpochDay = today - 2,
                isCancelled = true
            ),
            SubscriptionItem(
                id = 4,
                name = "Staged Trial",
                amount = 30.00,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.SOFTWARE,
                nextBillingEpochDay = today + 5,
                needsReview = true
            )
        )

        val summary = Calculations.summarize(items)
        // 15.00 + (120 / 12 = 10.00) = 25.00
        assertEquals(25.00, summary.monthlyRunRate, 0.01)
        assertEquals(300.00, summary.yearlyEquivalent, 0.01)
        assertEquals(2, summary.next30DaysCount)
        assertEquals(135.00, summary.next30DaysTotal, 0.01)
        assertEquals(1, summary.reviewQueueCount)
        assertEquals(1, summary.cancelledCount)
    }

    @Test
    fun icsGenerator_outputsValidRfc5545CalendarWithValarm() {
        val today = DateUtils.todayEpochDay()
        val items = listOf(
            SubscriptionItem(
                id = 10,
                name = "Spotify Duo",
                amount = 10.99,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.MUSIC,
                nextBillingEpochDay = today + 6
            ),
            SubscriptionItem(
                id = 11,
                name = "Old Gym",
                amount = 40.00,
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.FITNESS,
                nextBillingEpochDay = today - 5,
                isCancelled = true
            )
        )

        val ics = IcsGenerator.generateCalendarIcs(items, leadDays = 3)
        assertTrue(ics.contains("BEGIN:VCALENDAR"))
        assertTrue(ics.contains("VERSION:2.0"))
        assertTrue(ics.contains("SUMMARY:Renewal: Spotify Duo ($10.99)"))
        assertTrue(ics.contains("RRULE:FREQ=MONTHLY;INTERVAL=1"))
        assertTrue(ics.contains("BEGIN:VALARM"))
        assertTrue(ics.contains("TRIGGER:-P3D"))
        assertFalse(ics.contains("Old Gym"))
    }

    @Test
    fun jsonBackupManager_roundTripsLedgerPayload() {
        val today = DateUtils.todayEpochDay()
        val items = listOf(
            SubscriptionItem(
                id = 1,
                name = "ChatGPT Plus",
                amount = 20.0,
                currencyCode = "USD",
                frequency = BillingFrequency.MONTHLY,
                category = SubscriptionCategory.SOFTWARE,
                nextBillingEpochDay = today + 19
            )
        )

        val json = JsonBackupManager.exportToJson(items, currencyCode = "USD", alertLeadDays = 7)
        val parsed = JsonBackupManager.parseBackupJson(json).getOrThrow()
        assertEquals("USD", parsed.first.currencyCode)
        assertEquals(7, parsed.first.alertLeadDays)
        assertEquals(1, parsed.second.size)
        assertEquals("ChatGPT Plus", parsed.second.first().name)
        assertEquals(20.0, parsed.second.first().amount ?: 0.0, 0.01)
    }
}
