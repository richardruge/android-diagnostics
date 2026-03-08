package com.creative.core_system.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryInfoProviderImpl(
    private val context: Context
) : BatteryInfoProvider {

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

        return RawBatteryInfo(
            level = pct,
            temperatureC = temperatureC,
            isCharging = isCharging,
            chargeRateMah = null, // Android doesn't expose this directly
            health = health,
            capacityMah = null,   // optional: can be read from system files
            voltageMv = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1),
            technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        )
    }
}