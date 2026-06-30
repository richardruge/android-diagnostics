package com.creative.core_system.thermal

import android.content.Context
import android.os.PowerManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ThermalSystemDataSourceImpl(
    private val context: Context
) : ThermalSystemDataSource {

    private val powerManager by lazy {
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    override fun thermalStatus(): Flow<Int> = callbackFlow {
        val listener = PowerManager.OnThermalStatusChangedListener { status ->
            trySend(status)
        }

        powerManager.addThermalStatusListener(context.mainExecutor, listener)
        
        // Send initial status
        trySend(powerManager.currentThermalStatus)

        awaitClose {
            powerManager.removeThermalStatusListener(listener)
        }
    }
}
