package com.example.domain

import com.example.data.model.SubscriptionItem
import com.example.data.model.formatMoney

object IcsGenerator {

    fun generateCalendarIcs(subscriptions: List<SubscriptionItem>, leadDays: Int = 3): String {
        val activeItems = subscriptions.filter { !it.isCancelled }
        val nowStr = DateUtils.formatNowIcsUtc()

        val events = activeItems.joinToString("\r\n") { sub ->
            val startStr = DateUtils.formatIcsUtc(sub.nextBillingEpochDay)
            val amountLabel = if (sub.amount != null) {
                formatMoney(sub.amount, sub.currencyCode)
            } else {
                "Amount TBD"
            }
            val safeName = escapeIcsText(sub.name)
            val trialNote = if (sub.isTrial) " [Trial Ending]" else ""

            listOf(
                "BEGIN:VEVENT",
                "UID:subjogger-${sub.id}-${sub.nextBillingEpochDay}@subjogger.local",
                "DTSTAMP:$nowStr",
                "DTSTART:$startStr",
                "DURATION:PT30M",
                "SUMMARY:Renewal: $safeName ($amountLabel)$trialNote",
                "DESCRIPTION:SubJogger Offline Reminder: $safeName renews on this date. Review or cancel before charge.",
                "RRULE:FREQ=${sub.frequency.rruleFreq};INTERVAL=1",
                "BEGIN:VALARM",
                "TRIGGER:-P${leadDays}D",
                "ACTION:DISPLAY",
                "DESCRIPTION:Reminder: $safeName renews in $leadDays days!",
                "END:VALARM",
                "END:VEVENT"
            ).joinToString("\r\n")
        }

        return listOf(
            "BEGIN:VCALENDAR",
            "VERSION:2.0",
            "PRODID:-//SubJogger//Offline Subscription Reminders//EN",
            "CALSCALE:GREGORIAN",
            "METHOD:PUBLISH",
            events,
            "END:VCALENDAR"
        ).joinToString("\r\n")
    }

    private fun escapeIcsText(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }
}
