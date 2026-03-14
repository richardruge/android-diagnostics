package com.creative.core_data.thermal

import android.util.Log
import com.creative.core_model.ThermalSeverity
import com.creative.core_model.ThermalStatus
import com.creative.core_system.battery.BatteryInfoProvider
import com.creative.core_system.thermal.ThermalSystemDataSource

class ThermalRepositoryImpl(
    private val system: ThermalSystemDataSource,
    private val batteryProvider: BatteryInfoProvider
) : ThermalRepository {

    override suspend fun getThermalStatus(): ThermalStatus {
        val batteryInfo = batteryProvider.readCurrent()
        val temp = batteryInfo.temperatureC
        
        val severity = when {
            temp > 45f -> ThermalSeverity.CRITICAL
            temp > 40f -> ThermalSeverity.HOT
            temp > 35f -> ThermalSeverity.WARM
            else -> ThermalSeverity.NORMAL
        }

        Log.d("ThermalRepo", "Current battery temp: $temp, Severity: $severity")

        return ThermalStatus(
            status = "Success",
            temperatureC = temp,
            severity = severity
        )
    }
}
