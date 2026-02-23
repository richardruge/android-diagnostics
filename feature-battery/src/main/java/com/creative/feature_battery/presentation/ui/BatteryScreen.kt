package com.creative.feature_battery.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when {
            state.loading -> Text("Reading battery temperature…")
            state.error != null -> Text("Error: ${state.error}")
            else -> Text("Battery Temp: ${state.temperatureC} °C")
        }
    }
}