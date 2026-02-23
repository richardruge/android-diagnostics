package com.creative.feature_battery.presentation.ui

data class BatteryUiState(
    val temperatureC: Float? = null,
    val loading: Boolean = true,
    val error: String? = null
)