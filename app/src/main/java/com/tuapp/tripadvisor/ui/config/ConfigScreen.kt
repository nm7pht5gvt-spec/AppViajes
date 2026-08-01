package com.tuapp.tripadvisor.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tuapp.tripadvisor.service.overlay.OverlayService
import com.tuapp.tripadvisor.util.PermissionHelper

@Composable
fun ConfigScreen(
    viewModel: ConfigViewModel,
    onServiceActivated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var showOverlayDialog by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var awaitingSettingsReturn by remember { mutableStateOf(false) }

    fun attemptActivation() {
        when {
            !PermissionHelper.hasOverlayPermission(context) -> {
                showOverlayDialog = true
            }
            !PermissionHelper.isAccessibilityServiceEnabled(context) -> {
                showAccessibilityDialog = true
            }
            else -> {
                viewModel.saveAndActivate(onSuccess = {
                    OverlayService.start(context)
                    onServiceActivated()
                })
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && awaitingSettingsReturn) {
                awaitingSettingsReturn = false
                attemptActivation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DriverIQ",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF00B4D8)
        )
        Text(
            text = "Configuración de Tarifas Mínimas",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Límites por Kilómetro",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.pricePerKmInput,
                    onValueChange = { viewModel.onPricePerKmChanged(it) },
                    label = { Text("Precio mínimo por KM ($)") },
                    placeholder = { Text("Ej: 8.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Límites por Hora",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.earningsPerHourInput,
                    onValueChange = { viewModel.onEarningsPerHourChanged(it) },
                    label = { Text("Ganancia mínima por hora ($)") },
                    placeholder = { Text("Ej: 200.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        state.errorMessage?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (state.saveSuccess) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF00C853))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tarifas guardadas — Servicio Activo", fontSize = 14.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { attemptActivation() },
            enabled = !state.isSaving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    "Guardar y Activar DriverIQ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showOverlayDialog) {
        OverlayPermissionDialog(
            onConfirm = {
                showOverlayDialog = false
                awaitingSettingsReturn = true
                context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
            },
            onDismiss = { showOverlayDialog = false }
        )
    }

    if (showAccessibilityDialog) {
        AccessibilityPermissionDialog(
            onConfirm = {
                showAccessibilityDialog = false
                awaitingSettingsReturn = true
                context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
            },
            onDismiss = { showAccessibilityDialog = false }
        )
    }
}
