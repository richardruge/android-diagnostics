package com.creative.feature_battery.di

import androidx.room.Room
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
import com.creative.feature_battery.presentation.ui.chart.BatteryChartViewModel
import com.creative.feature_battery.presentation.ui.chart.BatteryLongTermViewModel
import com.creative.feature_battery.presentation.ui.debug.BatteryDebugViewModel
import com.creative.feature_battery.usage.ForegroundSessionManager
import com.creative.feature_battery.usage.UsagePermissionHelper
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val batteryFeatureModule = module {

    // Room database
    single {
        Room.databaseBuilder(
            get(),
            BatteryHistoryDatabase::class.java,
            "battery_history.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    // DAO
    single { get<BatteryHistoryDatabase>().dao() }
    single { get<BatteryHistoryDatabase>().aggregationDao() }

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
    single(createdAtStart = true) { ForegroundSessionManager(get(), get()) }
    single { UsagePermissionHelper(get()) }

    // Evaluator
    single { BatterySeverityEvaluator() }

    // Settings
    single<BatterySettingsRepository> { BatterySettingsRepositoryImpl(get()) }

    // ViewModels
    viewModelOf(::BatteryViewModel)
    viewModelOf(::BatterySettingsViewModel)
    viewModelOf(::BatteryChartViewModel)
    viewModelOf(::BatteryLongTermViewModel)
    viewModelOf(::BatteryDebugViewModel)
}
