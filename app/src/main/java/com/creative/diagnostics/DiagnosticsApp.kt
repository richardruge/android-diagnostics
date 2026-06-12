package com.creative.diagnostics

import android.app.Application
import com.creative.core_data.dataModule
import com.creative.core_system.battery.batterySystemModule
import com.creative.core_system.systemModule
import com.creative.feature_automation.di.automationFeatureModule
import com.creative.feature_battery.di.batteryFeatureModule
import com.creative.feature_network.di.networkFeatureModule
import com.creative.feature_thermal.di.thermalFeatureModule
import com.creative.feature_battery.data.history.BatteryHistoryDatabase
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        checkVersionAndClearData()
    }

    private fun checkVersionAndClearData() {
        val prefs = getSharedPreferences("app_metadata", MODE_PRIVATE)
        val savedVersion = prefs.getInt("last_app_version", -1)
        val currentVersion = BuildConfig.VERSION_CODE

        if (savedVersion != -1 && savedVersion < currentVersion) {
            Timber.i("Version update detected ($savedVersion -> $currentVersion). Clearing legacy data.")
            // Clear the battery history database on version increment
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = get<BatteryHistoryDatabase>()
                    db.clearAllTables()
                    Timber.i("Database cleared successfully.")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to clear database during version update")
                }
            }
        }
        
        prefs.edit().putInt("last_app_version", currentVersion).apply()
    }
}