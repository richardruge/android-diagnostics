package com.creative.feature_battery.domain.repository

import com.creative.feature_battery.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryHistoryRepository {
    suspend fun record(info: BatteryInfo)
    fun observeHistory(): Flow<List<BatteryInfo>>
    fun observeHistory(since: Long, limit: Int = 2000): Flow<List<BatteryInfo>>
    fun observeHistorySampled(since: Long, samplingRate: Int, limit: Int = 2000): Flow<List<BatteryInfo>>
    fun observeAggregatedHistory(since: Long): Flow<List<BatteryAggregation>>
    suspend fun clearHistory()
}

data class BatteryAggregation(
    val timestamp: Long,
    val avgLevel: Float,
    val avgTemperatureC: Float,
    val avgVoltageMv: Float?,
    val avgCurrentMa: Float?,
    val bucketDurationMinutes: Int
)
