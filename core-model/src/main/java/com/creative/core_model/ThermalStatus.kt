package com.creative.core_model

data class ThermalStatus(
    val status: String,
    val temperatureC: Float,
    val severity: ThermalSeverity,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ThermalSeverity {
    NORMAL, WARM, HOT, CRITICAL
}