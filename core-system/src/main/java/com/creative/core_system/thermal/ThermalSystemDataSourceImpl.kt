package com.creative.core_system.thermal

import android.content.Context
import android.os.Build
//import android.os.ThermalManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@RequiresApi(Build.VERSION_CODES.Q)
class ThermalSystemDataSourceImpl(
    private val context: Context
) : ThermalSystemDataSource {

    /*private val thermalManager by lazy {
        context.getSystemService(Context.THERMAL_SERVICE) as ThermalManager
    }*/

    override fun thermalStatus(): Flow<Int> = callbackFlow {
        /*val listener = ThermalManager.OnThermalStatusChangedListener { status ->
            trySend(status)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            thermalManager.addThermalStatusListener(context.mainExecutor, listener)
        } else {
            @Suppress("DEPRECATION")
            thermalManager.addThermalStatusListener(listener)
        }

        awaitClose {
            thermalManager.removeThermalStatusListener(listener)
        }*/
        awaitClose {}
    }
}
