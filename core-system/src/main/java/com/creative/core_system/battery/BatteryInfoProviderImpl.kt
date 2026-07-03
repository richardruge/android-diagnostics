package com.creative.core_system.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryInfoProviderImpl(
    private val context: Context
) : BatteryInfoProvider {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    override fun observe(): Flow<RawBatteryInfo> = callbackFlow {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        val receiver = context.registerReceiver(null, filter)
        receiver?.let { intent -> trySend(intent.toRawBatteryInfo()) }

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent != null) trySend(intent.toRawBatteryInfo())
            }
        }

        context.registerReceiver(broadcastReceiver, filter)

        awaitClose { context.unregisterReceiver(broadcastReceiver) }
    }

    override fun readCurrent(): RawBatteryInfo {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        return intent!!.toRawBatteryInfo()
    }

    @android.annotation.SuppressLint("PrivateApi")
    private fun getDesignCapacity(): Int? {
        val fromProfile = try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
            val batteryCapacity = powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double
            batteryCapacity.toInt().takeIf { it > 0 }
        } catch (_: Exception) {
            // PowerProfile is an internal API and may be restricted on some devices/Android versions
            null
        }

        return fromProfile ?: getDesignCapacityFromSysfs()
    }

    private fun getDesignCapacityFromSysfs(): Int? {
        val paths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/battery/capacity_design"
        )
        for (path in paths) {
            try {
                val file = java.io.File(path)
                if (file.exists()) {
                    val value = file.readText().trim().toLong()
                    if (value > 0) {
                        // charge_full_design is often in uAh (microampere-hours)
                        return if (value > 100_000) (value / 1000).toInt() else value.toInt()
                    }
                }
            } catch (_: Exception) {
                // Ignore and try next path
            }
        }
        return null
    }

    private fun Intent.toRawBatteryInfo(): RawBatteryInfo {
        val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        val tempTenths = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val temperatureC = if (tempTenths > 0) tempTenths / 10f else -1f

        val status = getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val health = when (getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            else -> BatteryHealth.UNSPECIFIED
        }

        val cycleCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getIntExtra("android.os.extra.CYCLE_COUNT", -1).takeIf { it != -1 }
        } else {
            null
        }

        val stateOfHealth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getIntExtra("android.os.extra.STATE_OF_HEALTH", -1).takeIf { it != -1 }
        } else {
            null
        } ?: getIntExtra("health_percentage", -1).takeIf { it != -1 }
        ?: getIntExtra("soh", -1).takeIf { it != -1 }

        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

        // Some devices don't provide current through BatteryManager but might have it in Intent extras
        val intentCurrentNow = getIntExtra("current_now", Int.MIN_VALUE)
        val effectiveCurrentNow = if (currentNow != Int.MIN_VALUE) currentNow else intentCurrentNow

        // Microamperes to Milliamperes
        val currentNowMa = if (effectiveCurrentNow != Int.MIN_VALUE) effectiveCurrentNow / 1000 else null
        val currentAverageMa = if (currentAverage != Int.MIN_VALUE) currentAverage / 1000 else null
        
        // Microampere-hours to Milliampere-hours
        val capacityMah = if (chargeCounter != Int.MIN_VALUE && chargeCounter > 0) chargeCounter / 1000 else null

        // Extraction of Max Charging Rate (API 23+)
        val maxChargingCurrent = getIntExtra("max_charging_current", -1)
        val maxChargingVoltage = getIntExtra("max_charging_voltage", -1)
        
        val voltageMv = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1).takeIf { it != -1 }

        return RawBatteryInfo(
            level = pct,
            temperatureC = temperatureC,
            isCharging = isCharging,
            chargeRateMah = null, 
            health = health,
            capacityMah = capacityMah,
            designCapacityMah = getDesignCapacity(),
            voltageMv = voltageMv,
            technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
            cycleCount = cycleCount,
            stateOfHealth = stateOfHealth,
            currentNowMa = currentNowMa,
            currentAverageMa = currentAverageMa,
            maxChargingCurrentUa = if (maxChargingCurrent != -1) maxChargingCurrent else null,
            maxChargingVoltageMv = if (maxChargingVoltage != -1) maxChargingVoltage else null
        )
    }
}
