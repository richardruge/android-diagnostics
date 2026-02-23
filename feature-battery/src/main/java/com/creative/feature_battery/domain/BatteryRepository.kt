package com.creative.feature_battery.domain

import com.creative.core_data.battery.BatteryInfoProvider
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun batteryTemperatureC(): Flow<Float>
}

class BatteryRepositoryImpl(
    private val provider: BatteryInfoProvider
) : BatteryRepository {

override fun batteryTemperatureC(): Flow<Float> =
    provider.batteryTemperatureC()
}