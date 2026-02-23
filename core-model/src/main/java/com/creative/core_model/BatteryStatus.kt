package com.creative.core_model

data class BatteryStatus(
    val levelPercent: Int,
    val health: BatteryHealth,
    val isCharging: Boolean
)

enum class BatteryHealth {
    GOOD, FAIR, POOR, UNKNOWN
}
