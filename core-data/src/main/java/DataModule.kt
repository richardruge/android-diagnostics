package com.creative.core_data

import com.creative.core_data.automation.AutomationRepository
import com.creative.core_data.automation.AutomationRepositoryImpl
import com.creative.core_data.battery.BatteryRepository
import com.creative.core_data.battery.BatteryRepositoryImpl
import com.creative.core_data.network.NetworkRepository
import com.creative.core_data.network.NetworkRepositoryImpl
import com.creative.core_data.thermal.ThermalRepository
import com.creative.core_data.thermal.ThermalRepositoryImpl
import org.koin.dsl.module

val dataModule = module {

    single<BatteryRepository> {
        BatteryRepositoryImpl(get())
    }

    single<NetworkRepository> {
        NetworkRepositoryImpl(get())
    }

    single<ThermalRepository> {
        ThermalRepositoryImpl(get())
    }

    single<AutomationRepository> {
        AutomationRepositoryImpl(get())
    }
}