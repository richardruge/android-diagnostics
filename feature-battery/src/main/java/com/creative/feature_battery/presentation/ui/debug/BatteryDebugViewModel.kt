package com.creative.feature_battery.presentation.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.model.BatteryHealthUi
import com.creative.feature_battery.domain.model.BatteryInfo
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

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    fun seedMockData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val twelveHoursAgo = now - (12 * 60 * 60 * 1000)

            val interval = 5 * 60 * 1000 // every 5 minutes
            var currentTimestamp = twelveHoursAgo

            while (currentTimestamp <= now) {
                val hoursSinceStart = (currentTimestamp - twelveHoursAgo) / (1000.0 * 60 * 60)
                val level = (100 - (hoursSinceStart * 8)).toInt().coerceIn(0, 100)
                val temp = 30f + 5f * sin(hoursSinceStart).toFloat()

                val info = BatteryInfo(
                    level = level,
                    temperatureC = temp,
                    isCharging = false,
                    chargeRateMah = null,
                    health = BatteryHealthUi.GOOD,
                    capacityMah = 5000,
                    voltageMv = 3700 + (level * 5),
                    technology = "Li-ion",
                    cycleCount = 100,
                    stateOfHealth = 99,
                    currentNowMa = -250,
                    currentAverageMa = -240,
                    maxChargingCurrentUa = null,
                    maxChargingVoltageMv = null,
                    timestamp = currentTimestamp
                )

                historyRepository.record(info)
                currentTimestamp += interval
            }
        }
    }
}
