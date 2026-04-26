package com.creative.core_data.thermal

import android.os.PowerManager
import com.creative.core_model.ThermalSeverity
import com.creative.core_model.ThermalStatus
import com.creative.core_system.battery.BatteryInfoProvider
import com.creative.core_system.thermal.ThermalSystemDataSource
import kotlinx.coroutines.flow.first

class ThermalRepositoryImpl(
    private val system: ThermalSystemDataSource,
    private val batteryProvider: BatteryInfoProvider
) : ThermalRepository {

    override suspend fun getThermalStatus(): ThermalStatus {
        val batteryInfo = batteryProvider.readCurrent()
        val temp = batteryInfo.temperatureC
        
        val thermalCode = try {
            system.thermalStatus().first()
        } catch (e: Exception) {
            -1
        }

        val severity = when (thermalCode) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalSeverity.NORMAL
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalSeverity.WARM
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalSeverity.WARM
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalSeverity.HOT
            PowerManager.THERMAL_STATUS_CRITICAL -> ThermalSeverity.CRITICAL
            PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalSeverity.CRITICAL
            PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalSeverity.CRITICAL
            else -> {
                // Fallback to battery temp if system thermal status is unavailable
                when {
                    temp > 45f -> ThermalSeverity.CRITICAL
                    temp > 40f -> ThermalSeverity.HOT
                    temp > 35f -> ThermalSeverity.WARM
                    else -> ThermalSeverity.NORMAL
                }
            }
        }

        return ThermalStatus(
            status = if (thermalCode != -1) "System Active" else "Estimated (Battery)",
            temperatureC = temp,
            severity = severity
        )
    }
}
