package com.tuapp.tripadvisor.util

import com.tuapp.tripadvisor.domain.model.TripOffer

object ScreenTextParser {
    fun parse(text: String): TripOffer? {
        if (text.isBlank()) return null

        val cleanText = text.replace("\n", " ").replace(",", ".")

        // 1. Extraer Pago Total (ej: $213.64)
        val priceRegex = """\$\s*(\d+(?:\.\d+)?)""".toRegex()
        val priceMatches = priceRegex.findAll(cleanText).mapNotNull { 
            it.groupValues[1].toDoubleOrNull() 
        }.toList()

        // El precio suele ser la cifra más alta con $ (para no confundir con $7.29/km)
        val earnings = priceMatches.maxOrNull() ?: return null

        // 2. Extraer y sumar TODAS las distancias encontradas (ej: 0.4 km + 28.8 km = 29.2 km)
        val distanceRegex = """(\d+(?:\.\d+)?)\s*km""".toRegex(RegexOption.IGNORE_CASE)
        val distances = distanceRegex.findAll(cleanText).mapNotNull { match ->
            val value = match.groupValues[1].toDoubleOrNull()
            // Ignorar el precio estimado por km si venía en formato "$7.29/km"
            if (value != null && !match.value.contains("$")) value else null
        }.toList()

        val totalDistanceKm = if (distances.isNotEmpty()) distances.sum() else return null

        // 3. Extraer Duración Total convirtiendo Horas + Minutos a Minutos totales (ej: "1 h 23 min" -> 83 min)
        var totalMinutes = 0.0
        val hoursRegex = """(\d+)\s*h""".toRegex(RegexOption.IGNORE_CASE)
        val minsRegex = """(\d+)\s*min""".toRegex(RegexOption.IGNORE_CASE)

        val hoursMatch = hoursRegex.find(cleanText)
        val minsMatches = minsRegex.findAll(cleanText).mapNotNull { it.groupValues[1].toDoubleOrNull() }.toList()

        if (hoursMatch != null) {
            totalMinutes += (hoursMatch.groupValues[1].toDoubleOrNull() ?: 0.0) * 60.0
        }
        if (minsMatches.isNotEmpty()) {
            totalMinutes += minsMatches.sum()
        }

        if (earnings > 0 && totalDistanceKm > 0 && totalMinutes > 0) {
            return TripOffer(
                earnings = earnings,
                distanceKm = totalDistanceKm,
                durationMinutes = totalMinutes
            )
        }

        return null
    }
}
