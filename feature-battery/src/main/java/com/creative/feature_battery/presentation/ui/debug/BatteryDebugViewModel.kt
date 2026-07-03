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

    fun seedMockAggregations() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val startTime = now - (30L * 24 * 60 * 60 * 1000)
            val bucketMinutes = 60
            val interval = bucketMinutes * 60 * 1000L
            
            val aggregations = mutableListOf<BatteryAggregation>()
            var currentTimestamp = startTime

            while (currentTimestamp <= now) {
                val hoursSinceStart = (currentTimestamp - startTime) / (1000.0 * 60 * 60)
                val level = (60 + 40 * sin(hoursSinceStart * 0.2)).toFloat().coerceIn(0f, 100f)
                val temp = 30f + 5f * sin(hoursSinceStart * 0.5).toFloat()

                aggregations.add(
                    BatteryAggregation(
                        timestamp = currentTimestamp,
                        avgLevel = level,
                        avgTemperatureC = temp,
                        avgVoltageMv = 3700f + (level * 5),
                        avgCurrentMa = if (sin(hoursSinceStart * 0.1) > 0) 1400f else -240f,
                        bucketDurationMinutes = bucketMinutes
                    )
                )
                currentTimestamp += interval
            }
            historyRepository.recordAggregations(aggregations)
        }
    }

    fun seedMockData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            // Seed 30 days of data to properly test Month view
            val startTime = now - (30L * 24 * 60 * 60 * 1000)

            val interval = 15 * 60 * 1000 // every 15 minutes
            var currentTimestamp = startTime

            while (currentTimestamp <= now) {
                val hoursSinceStart = (currentTimestamp - startTime) / (1000.0 * 60 * 60)
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
                    designCapacityMah = 5050,
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
