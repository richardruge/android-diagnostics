package com.creative.feature_battery.presentation.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_data.thermal.ThermalRepository
import com.creative.feature_battery.domain.BatterySeverityEvaluator
import com.creative.feature_battery.domain.model.BatteryHealthUi
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.Severity
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

enum class TimeWindow(val minutes: Long) {
    MIN_15(15),
    HOUR_1(60),
    HOUR_24(1440)
}

data class ChartUiState(
    val data: List<BatteryInfo> = emptyList(),
    val window: TimeWindow = TimeWindow.HOUR_1,
    val endTimestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryChartViewModel(
    private val historyRepository: BatteryHistoryRepository,
    batteryRepository: BatteryRepository,
    private val thermalRepository: ThermalRepository,
    private val evaluator: BatterySeverityEvaluator
) : ViewModel() {

    private val debugSeedMockData = false

    private val _selectedWindow = MutableStateFlow(TimeWindow.HOUR_1)
    val selectedWindow: StateFlow<TimeWindow> = _selectedWindow

    val batteryLevelModelProducer = CartesianChartModelProducer()
    val temperatureModelProducer = CartesianChartModelProducer()
    val voltageModelProducer = CartesianChartModelProducer()
    val currentModelProducer = CartesianChartModelProducer()

    val chartUiState: StateFlow<ChartUiState> = _selectedWindow.flatMapLatest { window ->
        val cutoff = System.currentTimeMillis() - (window.minutes * 60 * 1000)
        val samplingRate = when (window) {
            TimeWindow.HOUR_24 -> 10 // Downsample to every 10th point for 24h view
            TimeWindow.HOUR_1 -> 2   // Every 2nd point for 1h
            else -> 1
        }
        
        // Fetch sampled data with a safety limit to prevent CursorWindow size crashes (2MB limit)
        historyRepository.observeHistorySampled(cutoff, samplingRate, 2000).map { history ->
            val now = System.currentTimeMillis()
            val filtered = history.filter { it.temperatureC.isFinite() }
                .sortedBy { it.timestamp }
                .distinctBy { it.timestamp }
            ChartUiState(filtered, window, now)
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChartUiState()
    )

    val batteryStatus: StateFlow<BatteryInfo?> = batteryRepository.observeBatteryInfo()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
        
    val batteryHealthSeverity: StateFlow<Severity> = batteryStatus.map { info ->
        info?.let { evaluator.evaluate(it) } ?: Severity.NORMAL
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Severity.NORMAL
    )

    val thermalStatus = flow {
        while (true) {
            emit(thermalRepository.getThermalStatus())
            delay(5000.milliseconds)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    init {
        if (debugSeedMockData) {
            seedMockData()
        }

        viewModelScope.launch {
            chartUiState.collect { state ->
                if (state.data.size < 2) return@collect

                batteryLevelModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.data.map { it.timestamp.toDouble() },
                            state.data.map { it.level.toDouble() }
                        )
                    }
                }
                temperatureModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.data.map { it.timestamp.toDouble() },
                            state.data.map { it.temperatureC.toDouble() }
                        )
                    }
                }
                voltageModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.data.map { it.timestamp.toDouble() },
                            state.data.map { it.voltageMv?.toDouble() ?: 0.0 }
                        )
                    }
                }
                currentModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.data.map { it.timestamp.toDouble() },
                            state.data.map { it.currentNowMa?.toDouble() ?: 0.0 }
                        )
                    }
                }
            }
        }
    }

    fun setWindow(window: TimeWindow) {
        _selectedWindow.value = window
    }

    private fun seedMockData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            // Seed 30 days of data to properly test all trends views
            val startTime = now - (30L * 24 * 60 * 60 * 1000)

            val interval = 15 * 60 * 1000 // 15 minute intervals for faster seeding
            var currentTimestamp = startTime

            while (currentTimestamp <= now) {
                val hoursSinceStart = (currentTimestamp - startTime) / (1000.0 * 60 * 60)
                val dayOffset = (hoursSinceStart / 24).toInt()
                val hourOfDay = hoursSinceStart % 24
                
                // Simulate daily cycles
                val level = if (hourOfDay < 16) {
                    (100 - (hourOfDay * 5)).toInt().coerceIn(0, 100)
                } else {
                    (20 + ((hourOfDay - 16) * 10)).toInt().coerceIn(0, 100)
                }

                val temp = 30f + 5f * sin(hoursSinceStart * 0.5).toFloat()

                val info = BatteryInfo(
                    level = level,
                    temperatureC = temp,
                    isCharging = hourOfDay >= 16,
                    chargeRateMah = if (hourOfDay >= 16) 1500 else null,
                    health = BatteryHealthUi.GOOD,
                    capacityMah = 5000,
                    voltageMv = 3800 + (level * 4),
                    technology = "Li-ion",
                    cycleCount = 150 + dayOffset,
                    stateOfHealth = 98,
                    currentNowMa = if (hourOfDay >= 16) 1500 else -300,
                    currentAverageMa = if (hourOfDay >= 16) 1400 else -280,
                    maxChargingCurrentUa = 3000000,
                    maxChargingVoltageMv = 5000,
                    timestamp = currentTimestamp
                )

                historyRepository.record(info)
                currentTimestamp += interval
            }
            
            // Explicitly trigger aggregation after seeding to populate the long-term trends immediately
            historyRepository.runAggregation()
        }
    }
}
