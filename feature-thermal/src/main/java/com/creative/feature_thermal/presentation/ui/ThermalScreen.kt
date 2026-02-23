package com.creative.feature_thermal.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ThermalScreen(viewModel: ThermalViewModel = koinViewModel()) {
    val ui = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) { viewModel.load() }

    when {
        ui.isLoading ->
            Text("Reading thermal sensors…")

        ui.errorMessage != null ->
            Text("Error: ${ui.errorMessage}")

        ui.status != null && ui.temperatureC != null && ui.severity != null ->
            Text(
                "Thermal status: ${ui.status}\n" +
                        "Temperature: ${ui.temperatureC}°C\n" +
                        "Severity: ${ui.severity}"
            )
    }
}