package com.creative.core_data.battery

import android.os.BatteryManager
import com.creative.core_model.BatteryHealth
import com.creative.core_model.BatteryStatus
import com.creative.core_system.battery.BatterySystemDataSource

class BatteryRepositoryImpl(
    private val system: BatterySystemDataSource
) : BatteryRepository {

    override suspend fun getBatteryStatus(): BatteryStatus {
        val rawHealth = system.getBatteryHealth()
        val health = when (rawHealth) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT,
            BatteryManager.BATTERY_HEALTH_DEAD,
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.POOR
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealth.FAIR
            else -> BatteryHealth.UNKNOWN
        }

        return BatteryStatus(
            levelPercent = system.getBatteryLevel(),
            health = health,
            isCharging = system.isCharging(),
            capacityMah = system.getCapacityMah(),
            cycleCount = system.getCycleCount(),
            stateOfHealth = system.getStateOfHealth()
        )
    }
}
