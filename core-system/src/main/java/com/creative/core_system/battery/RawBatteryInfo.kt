package com.creative.core_system.battery

data class RawBatteryInfo(
    val level: Int,
    val temperatureC: Float,
    val isCharging: Boolean,
    val chargeRateMah: Int?,
    val health: BatteryHealth,
    val capacityMah: Int?,
    val voltageMv: Int?,
    val technology: String?
)