package com.creative.core_system

import com.creative.core_system.battery.*
import com.creative.core_system.network.*
import com.creative.core_system.thermal.*
import com.creative.core_system.usage.*
import org.koin.dsl.module

val systemModule = module {

    single<UsageSystemDataSource> {
        UsageSystemDataSourceImpl(get())
    }

    single<BatterySystemDataSource> {
        BatterySystemDataSourceImpl(get())
    }

    single<NetworkSystemDataSource> {
        NetworkSystemDataSourceImpl(get())
    }

    single<ThermalSystemDataSource> {
        ThermalSystemDataSourceImpl(get())
    }
}