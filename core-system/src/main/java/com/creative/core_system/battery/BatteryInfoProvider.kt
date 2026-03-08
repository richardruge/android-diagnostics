package com.creative.core_system.battery

import kotlinx.coroutines.flow.Flow

interface BatteryInfoProvider {
    fun observe(): Flow<RawBatteryInfo>
    fun readCurrent(): RawBatteryInfo
}
