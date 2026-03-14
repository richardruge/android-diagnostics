package com.creative.diagnostics

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import com.creative.core_system.systemModule
import com.creative.core_system.battery.batterySystemModule
import com.creative.core_data.dataModule
import com.creative.feature_automation.di.automationFeatureModule
import com.creative.feature_battery.di.batteryFeatureModule
import com.creative.feature_network.di.networkFeatureModule
import com.creative.feature_thermal.di.thermalFeatureModule


class DiagnosticsApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DiagnosticsApp)
            modules(
                systemModule,
                batterySystemModule,
                dataModule,
                batteryFeatureModule,
                networkFeatureModule,
                thermalFeatureModule,
                automationFeatureModule
            )
        }
    }
}