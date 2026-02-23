package com.creative.core_data.battery

import com.creative.core_model.BatteryStatus

interface BatteryRepository {
    suspend fun getBatteryStatus(): BatteryStatus
}