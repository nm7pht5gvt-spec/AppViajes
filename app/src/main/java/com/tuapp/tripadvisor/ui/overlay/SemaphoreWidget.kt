package com.tuapp.tripadvisor.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun SemaphoreWidget(
    evaluation: TripEvaluation,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (status, text) = when (evaluation) {
        is TripEvaluation.Evaluated -> evaluation.status to "%.1f $/km | %.1f $/h".format(evaluation.pricePerKm, evaluation.earningsPerHour)
        TripEvaluation.Idle -> SemaphoreStatus.GREY to "Esperando viaje..."
        TripEvaluation.ParsingError -> SemaphoreStatus.RED to "Error al leer datos"
    }

    val backgroundColor = when (status) {
        SemaphoreStatus.GREEN -> Color(0xFF2E7D32)
        SemaphoreStatus.YELLOW -> Color(0xFFF57F17)
        SemaphoreStatus.RED -> Color(0xFFC62828)
        SemaphoreStatus.GREY -> Color(0xFF424242)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
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
    }
}
