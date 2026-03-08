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
    val timestamp: Long
)
