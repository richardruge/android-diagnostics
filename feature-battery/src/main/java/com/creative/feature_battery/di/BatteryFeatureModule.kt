package com.creative.feature_battery.di

import androidx.room.Room
import com.creative.feature_battery.data.BatteryRepositoryImpl
import com.creative.feature_battery.data.history.BatteryHistoryDatabase
import com.creative.feature_battery.data.history.BatteryHistoryRepositoryImpl
import com.creative.feature_battery.domain.BatterySeverityEvaluator
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.creative.feature_battery.presentation.BatteryViewModel
import com.creative.feature_battery.presentation.ui.chart.BatteryChartViewModel
import com.creative.feature_battery.presentation.ui.debug.BatteryDebugViewModel
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

    // Evaluator
    single { BatterySeverityEvaluator() }

    // ViewModels
    viewModelOf(::BatteryViewModel)
    viewModelOf(::BatteryChartViewModel)
    viewModelOf(::BatteryDebugViewModel)
}
