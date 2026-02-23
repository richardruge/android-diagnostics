package com.creative.feature_automation.di

import com.creative.feature_automation.domain.usecases.RunAutomationUseCase
import com.creative.feature_automation.presentation.viewmodel.AutomationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val automationFeatureModule = module {

    factory { RunAutomationUseCase(get()) }

    viewModel { AutomationViewModel(get()) }
}
