package com.creative.feature_battery.di

import com.creative.feature_battery.domain.BatteryRepository
import com.creative.feature_battery.domain.BatteryRepositoryImpl
import com.creative.feature_battery.presentation.ui.BatteryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val batteryFeatureModule = module {
    single<BatteryRepository> { BatteryRepositoryImpl(get()) }
    viewModel { BatteryViewModel(get()) }
}
