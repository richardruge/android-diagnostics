package com.creative.feature_battery.domain.repository

import com.creative.feature_battery.domain.model.BatterySettings
import kotlinx.coroutines.flow.Flow

interface BatterySettingsRepository {
    fun getSettings(): Flow<BatterySettings>
    suspend fun updateRetentionPeriod(months: Int)
    suspend fun updateIgnoreSystemProcesses(ignore: Boolean)
}
