package com.creative.core_system.battery

import org.koin.dsl.module

val batterySystemModule = module {
    single<BatteryInfoProvider> { BatteryInfoProviderImpl(get()) }
}