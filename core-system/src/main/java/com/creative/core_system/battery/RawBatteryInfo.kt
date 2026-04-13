package com.creative.core_system.battery

data class RawBatteryInfo(
    val level: Int,
    val temperatureC: Float,
    val isCharging: Boolean,
    val chargeRateMah: Int?,
    val health: BatteryHealth,
    val capacityMah: Int?,
    val voltageMv: Int?,
    val technology: String?,
    val cycleCount: Int?,
    val stateOfHealth: Int?,
    val currentNowMa: Int?,
    val currentAverageMa: Int?,
    val maxChargingCurrentUa: Int?,
    val maxChargingVoltageMv: Int?
)