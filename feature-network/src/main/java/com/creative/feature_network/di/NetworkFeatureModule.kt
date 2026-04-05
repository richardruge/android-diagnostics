package com.creative.feature_network.di

import com.creative.feature_network.domain.usecases.GetNetworkStateUseCase
import com.creative.feature_network.presentation.ui.NetworkViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val networkFeatureModule = module {

    factory { GetNetworkStateUseCase(get()) }

    viewModel { NetworkViewModel(get()) }
}