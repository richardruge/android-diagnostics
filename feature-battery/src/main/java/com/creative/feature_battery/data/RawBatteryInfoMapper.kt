package com.creative.feature_battery.data

import com.creative.core_system.battery.RawBatteryInfo
import com.creative.feature_battery.domain.model.BatteryInfo

fun RawBatteryInfo.toBatteryInfo(): BatteryInfo =
    BatteryInfo(
        level = level,
        temperatureC = temperatureC,
        isCharging = isCharging,
        chargeRateMah = chargeRateMah,
        health = health.toUi(),
        capacityMah = capacityMah,
        designCapacityMah = designCapacityMah,
        voltageMv = voltageMv,
        technology = technology,
        cycleCount = cycleCount,
        stateOfHealth = stateOfHealth,
        currentNowMa = currentNowMa,
        currentAverageMa = currentAverageMa,
        maxChargingCurrentUa = maxChargingCurrentUa,
        maxChargingVoltageMv = maxChargingVoltageMv,
        timestamp = System.currentTimeMillis()
    )
