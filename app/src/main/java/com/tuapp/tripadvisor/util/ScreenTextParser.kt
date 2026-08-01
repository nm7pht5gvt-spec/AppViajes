package com.tuapp.tripadvisor.util

import com.tuapp.tripadvisor.domain.model.TripOffer

object ScreenTextParser {
    fun parse(text: String): TripOffer? {
        if (text.isBlank()) return null

        // Limpiar el texto para facilitar la lectura
        val cleanText = text.replace("\n", " ").replace(",", ".")

        // Regex para buscar el precio (ej: $120.50, $85)
        val priceRegex = """\$\s*(\d+(?:\.\d+)?)""".toRegex()
        // Regex para buscar la distancia (ej: 5.2 km, 12 km)
        val distanceRegex = """(\d+(?:\.\d+)?)\s*km""".toRegex(RegexOption.IGNORE_CASE)
        // Regex para buscar la duración (ej: 15 min, 20 mins)
        val durationRegex = """(\d+(?:\.\d+)?)\s*min""".toRegex(RegexOption.IGNORE_CASE)

        val priceMatch = priceRegex.find(cleanText)
        val distanceMatch = distanceRegex.find(cleanText)
        val durationMatch = durationRegex.find(cleanText)

        if (priceMatch != null && distanceMatch != null && durationMatch != null) {
            val earnings = priceMatch.groupValues[1].toDoubleOrNull() ?: return null
            val distance = distanceMatch.groupValues[1].toDoubleOrNull() ?: return null
            val duration = durationMatch.groupValues[1].toDoubleOrNull() ?: return null

            return TripOffer(
                earnings = earnings,
                distanceKm = distance,
                durationMinutes = duration
            )
        }

        return null
    }
}
