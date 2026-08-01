package com.tuapp.tripadvisor.util

import com.tuapp.tripadvisor.domain.model.TripOffer

object ScreenTextParser {

    fun isCurrentlyInTrip(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("en camino") || 
               lower.contains("en viaje") || 
               lower.contains("llevando a") || 
               lower.contains("dirígete a") ||
               lower.contains("hacia el destino") ||
               lower.contains("pasajero a bordo")
    }

    fun parse(text: String): TripOffer? {
        if (text.isBlank()) return null
        val cleanText = text.replace("\n", " ").replace(",", ".")

        // 1. Extraer Precio (Monto Principal con $)
        val priceRegex = """(?:\$|MXN\s*)\s*(\d+(?:\.\d+)?)""".toRegex(RegexOption.IGNORE_CASE)
        val priceMatches = priceRegex.findAll(cleanText).mapNotNull { 
            it.groupValues[1].toDoubleOrNull() 
        }.toList()

        // El precio de la oferta suele ser el valor mayor si hay varios
        val earnings = priceMatches.maxOrNull() ?: return null

        // 2. Extraer Distancias tanto en Kilómetros (km) como en Metros (m) - Caso DiDi
        var totalDistanceKm = 0.0

        // A) Buscar distancias en KM (ej: 3.2 km, 3.2km)
        val kmRegex = """(\d+(?:\.\d+)?)\s*km""".toRegex(RegexOption.IGNORE_CASE)
        val kmMatches = kmRegex.findAll(cleanText).mapNotNull { match ->
            val value = match.groupValues[1].toDoubleOrNull()
            if (value != null && !match.value.contains("$")) value else null
        }.toList()

        if (kmMatches.isNotEmpty()) {
            totalDistanceKm += kmMatches.take(2).sum()
        }

        // B) Buscar distancias en METROS (ej: 892 m, 889m) y convertirlas a KM (/1000)
        val mRegex = """(\d+)\s*m\b""".toRegex(RegexOption.IGNORE_CASE)
        val mMatches = mRegex.findAll(cleanText).mapNotNull { match ->
            val value = match.groupValues[1].toDoubleOrNull()
            // Ignorar "min" o coincidencia con texto
            if (value != null && !match.value.contains("min", true)) value else null
        }.toList()

        if (mMatches.isNotEmpty()) {
            // Convertir metros a kilómetros
            val metersInKm = mMatches.take(2).sum() / 1000.0
            totalDistanceKm += metersInKm
        }

        // 3. Extraer Tiempos (Horas y Minutos)
        var totalMinutes = 0.0
        val hoursRegex = """(\d+)\s*h""".toRegex(RegexOption.IGNORE_CASE)
        val minsRegex = """(\d+)\s*min""".toRegex(RegexOption.IGNORE_CASE)

        val hoursMatch = hoursRegex.find(cleanText)
        val minsMatches = minsRegex.findAll(cleanText).mapNotNull { it.groupValues[1].toDoubleOrNull() }.toList()

        if (hoursMatch != null) {
            totalMinutes += (hoursMatch.groupValues[1].toDoubleOrNull() ?: 0.0) * 60.0
        }
        if (minsMatches.isNotEmpty()) {
            totalMinutes += minsMatches.take(2).sum()
        }

        // Estimación rápida si solo hay distancia
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
