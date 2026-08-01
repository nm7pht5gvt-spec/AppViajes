package com.tuapp.tripadvisor.util

import com.tuapp.tripadvisor.domain.model.TripOffer

object ScreenTextParser {

    fun isCurrentlyInTrip(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("en camino") || 
               lower.contains("en viaje") || 
               lower.contains("llevando a") || 
               lower.contains("dirígete a") ||
               lower.contains("hacia el destino")
    }

    fun parse(text: String): TripOffer? {
        if (text.isBlank()) return null
        val cleanText = text.replace("\n", " ").replace(",", ".")

        // Regex para montos en Uber, DiDi e InDrive ($120.50, MXN 85, $90.00)
        val priceRegex = """(?:\$|MXN\s*)\s*(\d+(?:\.\d+)?)""".toRegex(RegexOption.IGNORE_CASE)
        val priceMatches = priceRegex.findAll(cleanText).mapNotNull { 
            it.groupValues[1].toDoubleOrNull() 
        }.toList()

        val earnings = priceMatches.maxOrNull() ?: return null

        // Extraer distancias (km)
        val distanceRegex = """(\d+(?:\.\d+)?)\s*km""".toRegex(RegexOption.IGNORE_CASE)
        val distances = distanceRegex.findAll(cleanText).mapNotNull { match ->
            val value = match.groupValues[1].toDoubleOrNull()
            if (value != null && !match.value.contains("$")) value else null
        }.toList()

        val totalDistanceKm = if (distances.isNotEmpty()) distances.sum() else return null

        // Extraer tiempos (horas y minutos)
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

        // Si solo encontramos distancia pero no tiempo explícito (ej. InDrive rápido), estimar 2.5 min por km
        if (totalMinutes == 0.0 && totalDistanceKm > 0) {
            totalMinutes = totalDistanceKm * 2.5
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
