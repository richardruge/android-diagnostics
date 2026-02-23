package com.creative.core_system

import android.os.Build
import com.creative.core_system.battery.*
import com.creative.core_system.network.*
import com.creative.core_system.thermal.*
import org.koin.dsl.module

val systemModule = module {

    single<BatterySystemDataSource> {
        BatterySystemDataSourceImpl(get())
    }

    single<NetworkSystemDataSource> {
        NetworkSystemDataSourceImpl(get())
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        single<ThermalSystemDataSource> {
            ThermalSystemDataSourceImpl(get())
        }
    }
}