package com.creative.feature_network.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkScreen(viewModel: NetworkViewModel = koinViewModel()) {
    val ui = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) { viewModel.load() }

    when {
        ui.isLoading ->
            Text("Checking network…")

        ui.errorMessage != null ->
            Text("Error: ${ui.errorMessage}")

        ui.levelPercent != null && ui.health != null && ui.isCharging != null ->
            Text(
                "Network level: ${ui.levelPercent}%\n" +
                        "Health: ${ui.health}\n" +
                        "Charging: ${ui.isCharging}"
            )
    }
}