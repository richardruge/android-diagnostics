package com.creative.feature_battery.domain.repository

import com.creative.feature_battery.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryHistoryRepository {
    suspend fun record(info: BatteryInfo)
    fun observeHistory(): Flow<List<BatteryInfo>>
    fun observeHistory(since: Long, limit: Int = 2000): Flow<List<BatteryInfo>>
    fun observeHistorySampled(since: Long, samplingRate: Int, limit: Int = 2000): Flow<List<BatteryInfo>>
}
