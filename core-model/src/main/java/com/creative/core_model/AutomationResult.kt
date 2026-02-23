package com.creative.core_model

data class AutomationResult(
    val success: Boolean,
    val message: String,
    val levelPercent: Float,
    val health: AutomationHealth,
    val isCharging: Boolean
)

enum class AutomationHealth {
    GOOD, FAIR, POOR, UNKNOWN
}
