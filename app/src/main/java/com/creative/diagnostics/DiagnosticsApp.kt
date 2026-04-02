package com.creative.diagnostics

import android.app.Application
import com.creative.core_data.dataModule
import com.creative.core_system.battery.batterySystemModule
import com.creative.core_system.systemModule
import com.creative.feature_automation.di.automationFeatureModule
import com.creative.feature_battery.di.batteryFeatureModule
import com.creative.feature_network.di.networkFeatureModule
import com.creative.feature_thermal.di.thermalFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class DiagnosticsApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Setup global exception handler to catch and log any unhandled crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught Exception in thread ${thread.name}")
            // Optional: You could write this to a file or a persistent store here if needed
            defaultHandler?.uncaughtException(thread, throwable)
        }

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