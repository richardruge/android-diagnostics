package com.creative.core_system.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

class BatterySystemDataSourceImpl(
    private val context: Context
) : BatterySystemDataSource {

    private val batteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    override fun getBatteryLevel(): Int {
        return batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY
        )
    }

    override fun getBatteryHealth(): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
    }

    override fun isCharging(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    override fun getCapacityMah(): Int? {
        // BATTERY_PROPERTY_CHARGE_COUNTER returns remaining energy in microampere-hours
        val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        return if (chargeCounter != Int.MIN_VALUE && chargeCounter != 0) {
            chargeCounter / 1000 // Convert to mAh
        } else {
            null
        }
    }

    override fun getCycleCount(): Int? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)?.takeIf { it != -1 }
        } else {
            null
        }
    }

    override fun getStateOfHealth(): Int? {
        // State of Health (SOH) is not directly exposed as a standard property in all Android versions
        // Some manufacturers might expose it, but for a generic implementation, we might leave it as null
        // or try to find a system-specific way.
        // In API 34+, there are some hidden or manufacturer-specific extras.
        return null
    }
}
