package com.creative.core_data.battery

import com.creative.core_model.BatteryHealth
import com.creative.core_model.BatteryStatus
import com.creative.core_system.battery.BatterySystemDataSource

class BatteryRepositoryImpl(
    private val system: BatterySystemDataSource
) : BatteryRepository {

    override suspend fun getBatteryStatus(): BatteryStatus {
        // Phase 0 placeholder
        return BatteryStatus(
            levelPercent = 0,
            health = BatteryHealth.UNKNOWN,
            isCharging = false
        )
    }
}