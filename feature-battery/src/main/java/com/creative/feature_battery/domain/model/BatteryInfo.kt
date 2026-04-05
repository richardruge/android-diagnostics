package com.creative.feature_battery.domain.model

data class BatteryInfo(
    val level: Int,
    val temperatureC: Float,
    val isCharging: Boolean,
    val chargeRateMah: Int?,
    val health: BatteryHealthUi,
    val capacityMah: Int?,
    val voltageMv: Int?,
    val technology: String?,
    val cycleCount: Int?,
    val stateOfHealth: Int?,
    val currentNowMa: Int?,
    val currentAverageMa: Int?,
    val timestamp: Long
)
