package com.creative.feature_battery.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.creative.feature_battery.data.BatteryRepositoryImpl
import com.creative.feature_battery.data.BatterySettingsRepositoryImpl
import com.creative.feature_battery.data.history.BatteryHistoryDatabase
import com.creative.feature_battery.data.history.BatteryHistoryRepositoryImpl
import com.creative.feature_battery.domain.BatterySeverityEvaluator
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.creative.feature_battery.domain.repository.BatterySettingsRepository
import com.creative.feature_battery.presentation.BatterySettingsViewModel
import com.creative.feature_battery.presentation.BatteryViewModel
import com.creative.feature_battery.presentation.AppDischargeViewModel
import com.creative.feature_battery.presentation.ui.chart.BatteryChartViewModel
import com.creative.feature_battery.presentation.ui.chart.BatteryLongTermViewModel
import com.creative.feature_battery.presentation.ui.debug.BatteryDebugViewModel
import com.creative.feature_battery.usage.ForegroundSessionManager
import com.creative.feature_battery.usage.UsagePermissionHelper
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val batteryFeatureModule = module {

    // Room database
    val migration7to8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create the new app_usage_history table added in version 8
            db.execSQL("CREATE TABLE IF NOT EXISTS `app_usage_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `avgMa` REAL NOT NULL, `totalMah` REAL NOT NULL)")
        }
    }

    single {
        Room.databaseBuilder(
            get(),
            BatteryHistoryDatabase::class.java,
            "battery_history.db"
        )
            .addMigrations(migration7to8)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }

    // DAO
    single { get<BatteryHistoryDatabase>().dao() }
    single { get<BatteryHistoryDatabase>().aggregationDao() }
    single { get<BatteryHistoryDatabase>().usageDao() }

    // History repository
    single<BatteryHistoryRepository> {
        BatteryHistoryRepositoryImpl(
            dao = get(),
            aggregationDao = get(),
            settingsRepository = get(),
            maxSize = 10_000
        )
    }

    // Main battery repository - Set to create at start to begin recording immediately
    single<BatteryRepository>(createdAtStart = true) {
        BatteryRepositoryImpl(
            provider = get(),
            historyRepository = get()
        ) 
    }

    // Usage Tracking
    single(createdAtStart = true) { ForegroundSessionManager(get(), get(), get(), get(), get()) }
    single { UsagePermissionHelper(get()) }

    // Evaluator
    single { BatterySeverityEvaluator() }

    // Settings
    single<BatterySettingsRepository> { BatterySettingsRepositoryImpl(get()) }

    // ViewModels
    viewModelOf(::BatteryViewModel)
    viewModelOf(::AppDischargeViewModel)
    viewModelOf(::BatterySettingsViewModel)
    viewModelOf(::BatteryChartViewModel)
    viewModelOf(::BatteryLongTermViewModel)
    viewModelOf(::BatteryDebugViewModel)
}
