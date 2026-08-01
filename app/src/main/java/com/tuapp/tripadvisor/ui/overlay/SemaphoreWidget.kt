package com.tuapp.tripadvisor.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.tripadvisor.domain.model.SemaphoreStatus
import com.tuapp.tripadvisor.domain.model.TripEvaluation
import com.tuapp.tripadvisor.domain.model.ZoneRisk

@Composable
fun SemaphoreWidget(
    evaluation: TripEvaluation,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (evaluation) {
        is TripEvaluation.Evaluated -> when (evaluation.status) {
            SemaphoreStatus.GREEN -> Color(0xFF1B5E20)  // Verde
            SemaphoreStatus.YELLOW -> Color(0xFFE65100) // Naranja
            SemaphoreStatus.RED -> Color(0xFFB71C1C)    // Rojo
            SemaphoreStatus.GREY -> Color(0xFF212121)
        }
        else -> Color(0xFF212121)
    }

    Box(
        modifier = modifier
            .width(220.dp)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            // Cabecera con botón de cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DriverIQ",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (evaluation) {
                is TripEvaluation.Evaluated -> {
                    // 1. Pago por Km
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$/KM: $${"%.1f".format(evaluation.pricePerKm)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (evaluation.passesKm) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (evaluation.passesKm) Color(0xFF69F0AE) else Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Pago por Hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$/Hora: $${"%.1f".format(evaluation.earningsPerHour)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (evaluation.passesHour) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (evaluation.passesHour) Color(0xFF69F0AE) else Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Evaluación de Zona
                    val (zoneText, zoneColor) = when (evaluation.zoneRisk) {
                        ZoneRisk.SAFE -> "Segura" to Color(0xFF69F0AE)
                        ZoneRisk.NORMAL -> "Normal" to Color(0xFFFFD700)
                        ZoneRisk.RISK -> "Riesgo ⚠️" to Color(0xFFFF5252)
                        ZoneRisk.UNKNOWN -> "Normal" to Color.White
                    }

                    Text(
                        text = "Zona: $zoneText",
                        color = zoneColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TripEvaluation.Idle -> {
                    Text(
                        text = "Esperando viaje...",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                TripEvaluation.ParsingError -> {
                    Text(
                        text = "Error al leer datos",
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
