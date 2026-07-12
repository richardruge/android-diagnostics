package com.creative.omnigauge

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.creative.core_data.dataModule
import com.creative.core_system.battery.batterySystemModule
import com.creative.core_system.systemModule
import com.creative.feature_automation.di.automationFeatureModule
import com.creative.feature_battery.di.batteryFeatureModule
import com.creative.feature_network.di.networkFeatureModule
import com.creative.feature_thermal.di.thermalFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class OmniGaugeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Set this device as a test device to avoid "No Fill" errors (Error Code 3)
        val testDeviceIds = listOf("F6B6DCE3CA5F574BC0D4274C5A862BE2")
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        MobileAds.initialize(this)

        // Setup global exception handler to catch and log any unhandled crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught Exception in thread ${thread.name}")
            // Optional: You could write this to a file or a persistent store here if needed
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            startKoin {
                // Log Koin events to logcat
                androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
                androidContext(this@OmniGaugeApp)
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
            Timber.i("Koin initialized successfully")
        } catch (e: Exception) {
            Timber.e(e, "CRITICAL: Koin failed to initialize. Modules or Context might be misconfigured. Stacktrace: ${e.stackTraceToString()}")
        }
    }
}