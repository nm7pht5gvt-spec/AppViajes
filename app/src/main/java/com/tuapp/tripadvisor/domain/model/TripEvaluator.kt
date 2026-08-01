package com.tuapp.tripadvisor.domain.model

import com.tuapp.tripadvisor.data.preferences.UserPreferences

class TripEvaluator {
    fun evaluate(offer: TripOffer, prefs: UserPreferences): TripEvaluation {
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

        return TripEvaluation.Evaluated(
            status = status,
            pricePerKm = pricePerKm,
            earningsPerHour = earningsPerHour
        )
    }
}
