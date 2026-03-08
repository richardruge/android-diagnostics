package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun BatteryChartScreen(viewModel: BatteryChartViewModel) {
    val data by viewModel.chartData.collectAsState()
    TemperatureChart(data.map { it.temperatureC })
}
