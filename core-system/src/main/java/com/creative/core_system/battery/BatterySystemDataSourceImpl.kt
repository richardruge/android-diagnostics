package com.creative.core_system.battery

import android.content.Context
import android.os.BatteryManager

class BatterySystemDataSourceImpl(
    context: Context
) : BatterySystemDataSource {

    private val batteryManager =
        context.getSystemService(BatteryManager::class.java)

    override fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )
    }

    override fun getBatteryHealth(): Int {
        // Placeholder — real implementation later
        return 0
    }
}