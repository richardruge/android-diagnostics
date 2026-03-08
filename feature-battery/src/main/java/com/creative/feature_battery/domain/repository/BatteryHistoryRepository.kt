package com.creative.feature_battery.domain.repository

import com.creative.feature_battery.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryHistoryRepository {
    suspend fun record(info: BatteryInfo)
    fun observeHistory(): Flow<List<BatteryInfo>>
}