package com.creative.core_model

data class BatteryStatus(
    val levelPercent: Int,
    val health: BatteryHealth,
    val isCharging: Boolean,
    val capacityMah: Int? = null,
    val cycleCount: Int? = null,
    val stateOfHealth: Int? = null // Often referred to as durability or battery life percentage
)

enum class BatteryHealth {
    GOOD, FAIR, POOR, UNKNOWN
}
