package com.creative.feature_battery.data

import com.creative.core_system.battery.RawBatteryInfo
import com.creative.feature_battery.domain.model.BatteryInfo

fun RawBatteryInfo.toBatteryInfo(): BatteryInfo =
    BatteryInfo(
        level = level,
        temperatureC = temperatureC,
        isCharging = isCharging,
        chargeRateMah = chargeRateMah,
        health = health.toUi(),   // if you already have a UI enum mapper
        capacityMah = capacityMah,
        voltageMv = voltageMv,
        technology = technology,
        timestamp = System.currentTimeMillis()
    )

