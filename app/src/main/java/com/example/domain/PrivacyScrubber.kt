package com.example.domain

data class ScrubResult(
    val scrubbedText: String,
    val redactedCount: Int,
    val cardMasked: Boolean,
    val emailObfuscated: Boolean,
    val phoneRedacted: Boolean,
    val idMasked: Boolean,
    val detectedLast4Hint: String
)

object PrivacyScrubber {
    private val ccRegex = Regex("""\b(?:\d[ -]*?){13,19}\b""")
    private val endingInRegex = Regex("""\b(ending\s+in|last\s*4[:\s]*)\s*(\d{4})\b""", RegexOption.IGNORE_CASE)
    private val emailRegex = Regex("""\b([a-zA-Z0-9_.+-])[a-zA-Z0-9_.+-]*@([a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+)\b""")
    private val phoneRegex = Regex("""\b(?:\+?1[-. ]?)?\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})\b""")
    private val orderIdRegex = Regex("""\b(?:order|inv|invoice|receipt|txn|ref)[\s#:]*([a-zA-Z0-9_-]{5,})\b""", RegexOption.IGNORE_CASE)

    fun scrubText(rawText: String): ScrubResult {
        if (rawText.isBlank()) {
            return ScrubResult(
                scrubbedText = "",
                redactedCount = 0,
                cardMasked = false,
                emailObfuscated = false,
                phoneRedacted = false,
                idMasked = false,
                detectedLast4Hint = ""
            )
        }

        var scrubbed = rawText
        var redactedCount = 0
        var cardMasked = false
        var emailObfuscated = false
        var phoneRedacted = false
        var idMasked = false
        var last4Hint = ""

        // 1. Credit card PANs -> [•••• 4821]
        scrubbed = ccRegex.replace(scrubbed) { match ->
            val digits = match.value.filter { it.isDigit() }
            if (digits.length in 13..19) {
                redactedCount++
                cardMasked = true
                val last4 = digits.takeLast(4)
                if (last4Hint.isEmpty()) last4Hint = "•••• $last4"
                "[•••• $last4]"
            } else {
                match.value
            }
        }

        // Also catch "ending in 4821" cues
        val endingMatch = endingInRegex.find(scrubbed)
        if (endingMatch != null) {
            cardMasked = true
            val digits = endingMatch.groupValues[2]
            if (last4Hint.isEmpty()) last4Hint = "•••• $digits"
            scrubbed = endingInRegex.replace(scrubbed) { "[•••• ${it.groupValues[2]}]" }
        }

        // 2. Email addresses -> s***@domain.com
        scrubbed = emailRegex.replace(scrubbed) { match ->
            redactedCount++
            emailObfuscated = true
            "${match.groupValues[1]}***@${match.groupValues[2]}"
        }

        // 3. Phone numbers -> ***-***-0199
        scrubbed = phoneRegex.replace(scrubbed) {
            redactedCount++
            phoneRedacted = true
            "***-***-0199"
        }

        // 4. Order and invoice IDs -> [MASKED_ID]
        scrubbed = orderIdRegex.replace(scrubbed) {
            redactedCount++
            idMasked = true
            "[MASKED_ID]"
        }

        return ScrubResult(
            scrubbedText = scrubbed,
            redactedCount = redactedCount,
            cardMasked = cardMasked,
            emailObfuscated = emailObfuscated,
            phoneRedacted = phoneRedacted,
            idMasked = idMasked,
            detectedLast4Hint = last4Hint
        )
    }
}
