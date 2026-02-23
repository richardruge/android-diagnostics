package com.creative.feature_thermal.di

import com.creative.core_data.thermal.ThermalRepository
import com.creative.feature_thermal.domain.usecases.GetThermalStatusUseCase
import com.creative.feature_thermal.presentation.viewmodel.ThermalViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val thermalFeatureModule = module {

    factory { GetThermalStatusUseCase(get<ThermalRepository>()) }
    viewModel { ThermalViewModel(get<GetThermalStatusUseCase>()) }
}
