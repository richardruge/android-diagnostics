package com.creative.core_data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface BatteryInfoProvider {
    fun batteryTemperatureC(): Flow<Float>
}

class BatteryInfoProviderImpl(
    private val context: Context
) : BatteryInfoProvider {

    override fun batteryTemperatureC(): Flow<Float> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val raw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
                if (raw > 0) trySend(raw / 10f)
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        awaitClose { context.unregisterReceiver(receiver) }
    }
}