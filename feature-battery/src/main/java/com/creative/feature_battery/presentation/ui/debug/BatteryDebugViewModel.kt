package com.creative.feature_battery.presentation.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.model.BatteryHealthUi
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.repository.BatteryAggregation
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sin

class BatteryDebugViewModel(
    private val historyRepository: BatteryHistoryRepository
) : ViewModel() {

    val history: StateFlow<List<BatteryInfo>> = historyRepository.observeHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val aggregations: StateFlow<List<BatteryAggregation>> = historyRepository.observeAggregatedHistory(0L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    fun seedMockData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)

            val interval = 15 * 60 * 1000 // every 15 minutes
            var currentTimestamp = sevenDaysAgo

            while (currentTimestamp <= now) {
                val hoursSinceStart = (currentTimestamp - sevenDaysAgo) / (1000.0 * 60 * 60)
                // Level fluctuates between 20 and 100
                val level = (60 + 40 * sin(hoursSinceStart * 0.2)).toInt().coerceIn(0, 100)
                val temp = 30f + 5f * sin(hoursSinceStart * 0.5).toFloat()

                val info = BatteryInfo(
                    level = level,
                    temperatureC = temp,
                    isCharging = sin(hoursSinceStart * 0.1) > 0,
                    chargeRateMah = if (sin(hoursSinceStart * 0.1) > 0) 1500 else null,
                    health = BatteryHealthUi.GOOD,
                    capacityMah = 5000,
                    voltageMv = 3700 + (level * 5),
                    technology = "Li-ion",
                    cycleCount = 100,
                    stateOfHealth = 99,
                    currentNowMa = if (sin(hoursSinceStart * 0.1) > 0) 1500 else -250,
                    currentAverageMa = if (sin(hoursSinceStart * 0.1) > 0) 1400 else -240,
                    maxChargingCurrentUa = null,
                    maxChargingVoltageMv = null,
                    timestamp = currentTimestamp
                )

                historyRepository.record(info)
                currentTimestamp += interval
            }
            // Force an aggregation pass so some data shows up in the Aggregated tab immediately
            historyRepository.runAggregation()
        }
    }
}
