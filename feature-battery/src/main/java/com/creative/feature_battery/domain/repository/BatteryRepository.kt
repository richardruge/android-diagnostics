package com.creative.feature_battery.domain.repository

import com.creative.feature_battery.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun observeBatteryInfo(): Flow<BatteryInfo>
    suspend fun currentBatteryInfo(): BatteryInfo
}