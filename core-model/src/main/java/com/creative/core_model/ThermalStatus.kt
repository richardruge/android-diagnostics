package com.creative.core_model

data class ThermalStatus(
    val status: String,
    val temperatureC: Float,
    val severity: ThermalSeverity
)

enum class ThermalSeverity {
    NORMAL, WARM, HOT, CRITICAL
}