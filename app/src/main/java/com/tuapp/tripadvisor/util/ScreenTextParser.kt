package com.tuapp.tripadvisor.util

import com.tuapp.tripadvisor.domain.model.TripOffer

object ScreenTextParser {
    fun parse(text: String): TripOffer? {
        if (text.isBlank()) return null

        val cleanText = text.replace("\n", " ").replace(",", ".")

        val priceRegex = """\$\s*(\d+(?:\.\d+)?)""".toRegex()
        val distanceRegex = """(\d+(?:\.\d+)?)\s*km""".toRegex(RegexOption.IGNORE_CASE)
        val durationRegex = """(\d+(?:\.\d+)?)\s*min""".toRegex(RegexOption.IGNORE_CASE)

        val priceMatch = priceRegex.find(cleanText)
        val distanceMatch = distanceRegex.find(cleanText)
        val durationMatch = durationRegex.find(cleanText)

        if (priceMatch != null && distanceMatch != null && durationMatch != null) {
            val earnings = priceMatch.groupValues[1].toDoubleOrNull() ?: return null
            val distance = distanceMatch.groupValues[1].toDoubleOrNull() ?: return null
            val duration = durationMatch.groupValues[1].toDoubleOrNull() ?: return null

            if (distance > 0 && duration > 0 && earnings > 0) {
                return TripOffer(
                    earnings = earnings,
                    distanceKm = distance,
                    durationMinutes = duration
                )
            }
        }

        return null
    }
}
