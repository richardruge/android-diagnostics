package com.creative.feature_thermal.presentation.ui

import com.creative.core_model.ThermalSeverity

data class ThermalUiState(
    val status: String? = null,
    val temperatureC: Float? = null,
    val severity: ThermalSeverity? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)