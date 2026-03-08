package com.creative.feature_battery.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creative.feature_battery.presentation.BatteryUiState
import com.creative.feature_battery.presentation.BatteryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is BatteryUiState.Loading -> LoadingView()
        is BatteryUiState.Ready -> BatteryContent(uiState as BatteryUiState.Ready)
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BatteryContent(state: BatteryUiState.Ready) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Battery Level: ${state.level}%")
        Text("Temperature: ${state.temperatureC}°C")
        Text("Charging: ${state.isCharging}")
        Text("Health: ${state.health}")
        Text("Severity: ${state.severity}")
        state.capacityMah?.let { Text("Capacity: $it mAh") }
        state.voltageMv?.let { Text("Voltage: $it mV") }
        state.technology?.let { Text("Tech: $it") }
    }
}