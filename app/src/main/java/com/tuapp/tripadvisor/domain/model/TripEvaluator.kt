package com.tuapp.tripadvisor.domain.model

import com.tuapp.tripadvisor.data.preferences.UserPreferences

class TripEvaluator {
    fun evaluate(offer: TripOffer, prefs: UserPreferences, destinationText: String = ""): TripEvaluation {
        if (offer.distanceKm <= 0.0 || offer.durationMinutes <= 0.0) {
            return TripEvaluation.ParsingError
        }

        val pricePerKm = offer.earnings / offer.distanceKm
        val earningsPerHour = (offer.earnings / offer.durationMinutes) * 60.0

        val passesKm = pricePerKm >= prefs.minPricePerKm
        val passesHour = earningsPerHour >= prefs.minEarningsPerHour

        val status = when {
            passesKm && passesHour -> SemaphoreStatus.GREEN
            passesKm || passesHour -> SemaphoreStatus.YELLOW
            else -> SemaphoreStatus.RED
        }

        val zoneRisk = evaluateZone(destinationText)

        return TripEvaluation.Evaluated(
            status = status,
            totalDistanceKm = offer.distanceKm,
            pricePerKm = pricePerKm,
            passesKm = passesKm,
            earningsPerHour = earningsPerHour,
            passesHour = passesHour,
            zoneRisk = zoneRisk,
            zoneName = if (destinationText.isBlank()) "Normal" else destinationText
        )
    }

    private fun evaluateZone(text: String): ZoneRisk {
        val lower = text.lowercase()
        val riskKeywords = listOf("tepito", "doctores", "iztapalapa", "ecatepec", "lagunilla", "renacimiento")
        val safeKeywords = listOf("polanco", "roma", "condesa", "del valle", "santa fe", "aeropuerto")

        return when {
            riskKeywords.any { lower.contains(it) } -> ZoneRisk.RISK
            safeKeywords.any { lower.contains(it) } -> ZoneRisk.SAFE
            else -> ZoneRisk.NORMAL
        }
    }
}
