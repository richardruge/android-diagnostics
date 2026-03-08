package com.creative.feature_battery.data

import com.creative.core_system.battery.BatteryHealth
import com.creative.feature_battery.domain.model.BatteryHealthUi

fun BatteryHealth.toUi(): BatteryHealthUi =
    when (this) {
        BatteryHealth.GOOD -> BatteryHealthUi.GOOD
        BatteryHealth.OVERHEAT -> BatteryHealthUi.OVERHEAT
        BatteryHealth.DEAD -> BatteryHealthUi.DEAD
        BatteryHealth.OVER_VOLTAGE -> BatteryHealthUi.OVER_VOLTAGE
        else -> BatteryHealthUi.UNSPECIFIED
    }