package com.creative.feature_battery.di

import androidx.room.Room
import com.creative.feature_battery.data.BatteryRepositoryImpl
import com.creative.feature_battery.data.history.BatteryHistoryDatabase
import com.creative.feature_battery.data.history.BatteryHistoryRepositoryImpl
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.creative.feature_battery.presentation.BatteryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlin.jvm.java

val batteryFeatureModule = module {

    // Room database
    single {
        Room.databaseBuilder(
            get(),
            BatteryHistoryDatabase::class.java,
            "battery_history.db"
        ).build()
    }

    // DAO
    single { get<BatteryHistoryDatabase>().dao() }

    // History repository
    single<BatteryHistoryRepository> {
        BatteryHistoryRepositoryImpl(
            dao = get(),
            maxSize = 10_000
        )
    }

    // Main battery repository
    single<BatteryRepository> { BatteryRepositoryImpl(get()) }

    // ViewModel
    viewModel { BatteryViewModel(get(), get()) }
}