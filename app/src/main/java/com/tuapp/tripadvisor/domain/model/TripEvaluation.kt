package com.tuapp.tripadvisor.domain.model

enum class SemaphoreStatus {
    GREEN, YELLOW, RED, GREY
}

enum class ZoneRisk {
    SAFE, NORMAL, RISK, UNKNOWN
}

sealed class TripEvaluation {
    object Idle : TripEvaluation()
    object ParsingError : TripEvaluation()
    data class Evaluated(
        val status: SemaphoreStatus,
        val pricePerKm: Double,
        val passesKm: Boolean,
        val earningsPerHour: Double,
        val passesHour: Boolean,
        val zoneRisk: ZoneRisk,
        val zoneName: String
    ) : TripEvaluation()
}
