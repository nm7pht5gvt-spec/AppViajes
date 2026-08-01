package com.tuapp.tripadvisor.domain.model

enum class SemaphoreStatus {
    GREEN, YELLOW, RED, GREY
}

sealed class TripEvaluation {
    object Idle : TripEvaluation()
    object ParsingError : TripEvaluation()
    data class Evaluated(
        val status: SemaphoreStatus,
        val pricePerKm: Double,
        val earningsPerHour: Double
    ) : TripEvaluation()
}
