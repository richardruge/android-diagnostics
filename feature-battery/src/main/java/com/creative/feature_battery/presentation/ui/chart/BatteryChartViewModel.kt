package com.creative.feature_battery.presentation.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_data.thermal.ThermalRepository
import com.creative.core_model.ThermalSeverity
import com.creative.feature_battery.domain.BatterySeverityEvaluator
import com.creative.feature_battery.domain.model.BatteryHealthUi
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.Severity
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sin

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

data class DiagnosticAlert(
    val title: String,
    val message: String,
    val severity: AlertSeverity
)

enum class AlertSeverity {
    INFO, WARNING, CRITICAL
}

class BatteryChartViewModel(
    private val historyRepository: BatteryHistoryRepository,
    private val batteryRepository: BatteryRepository,
    private val thermalRepository: ThermalRepository,
    private val evaluator: BatterySeverityEvaluator
) : ViewModel() {

    private val DEBUG_SEED_MOCK_DATA = true

    init {
        if (DEBUG_SEED_MOCK_DATA) {
            seedMockData()
        }
    }

    private val _selectedWindow = MutableStateFlow(TimeWindow.HOUR_1)
    val selectedWindow: StateFlow<TimeWindow> = _selectedWindow

    val batteryLevelModelProducer = CartesianChartModelProducer()
    val temperatureModelProducer = CartesianChartModelProducer()

    val chartUiState: StateFlow<ChartUiState> = combine(
        historyRepository.observeHistory(),
        _selectedWindow
    ) { history, window ->
        val now = System.currentTimeMillis()
        val cutoff = now - (window.minutes * 60 * 1000)
        val filtered = history.filter { 
            it.timestamp >= cutoff && it.temperatureC.isFinite()
        }
            .sortedBy { it.timestamp }
            .distinctBy { it.timestamp }
        
        ChartUiState(filtered, window, now)
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
            delay(5000)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val alerts: StateFlow<List<DiagnosticAlert>> = combine(thermalStatus, chartUiState) { status, state ->
        val activeAlerts = mutableListOf<DiagnosticAlert>()
        
        // 1. Thermal Throttling Alert
        if (status != null && (status.severity == ThermalSeverity.HOT || status.severity == ThermalSeverity.CRITICAL)) {
            activeAlerts.add(
                DiagnosticAlert(
                    title = "Thermal Throttling Detected",
                    message = "System is in ${status.severity} state. Performance may be reduced.",
                    severity = AlertSeverity.CRITICAL
                )
            )
        }

        // 2. Battery Temperature Alert (Threshold: 40°C)
        val latestTemp = status?.temperatureC ?: state.data.lastOrNull()?.temperatureC
        if (latestTemp != null && latestTemp > 40f) {
            activeAlerts.add(
                DiagnosticAlert(
                    title = "High Battery Temperature",
                    message = "Battery temperature is ${"%.1f".format(latestTemp)}°C, exceeding safe threshold of 40°C.",
                    severity = AlertSeverity.WARNING
                )
            )
        }

        activeAlerts
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun setWindow(window: TimeWindow) {
        _selectedWindow.value = window
    }

    private fun seedMockData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val twentyFourHoursAgo = now - (24 * 60 * 60 * 1000)

            val interval = 1 * 60 * 1000
            var currentTimestamp = twentyFourHoursAgo

            while (currentTimestamp <= now) {
                val hoursSinceStart = (currentTimestamp - twentyFourHoursAgo) / (1000.0 * 60 * 60)
                val level = if (hoursSinceStart < 16) {
                    (100 - (hoursSinceStart * 5)).toInt().coerceIn(0, 100)
                } else {
                    (20 + ((hoursSinceStart - 16) * 10)).toInt().coerceIn(0, 100)
                }

                val temp = 33f + 5f * sin(hoursSinceStart * 0.5).toFloat()

                val info = BatteryInfo(
                    level = level,
                    temperatureC = temp,
                    isCharging = hoursSinceStart >= 16,
                    chargeRateMah = if (hoursSinceStart >= 16) 1500 else null,
                    health = BatteryHealthUi.GOOD,
                    capacityMah = 5000,
                    voltageMv = 3800 + (level * 4),
                    technology = "Li-ion",
                    cycleCount = 150,
                    stateOfHealth = 98,
                    currentNowMa = if (hoursSinceStart >= 16) 1500 else -300,
                    currentAverageMa = if (hoursSinceStart >= 16) 1400 else -280,
                    maxChargingCurrentUa = 3000000,
                    maxChargingVoltageMv = 5000,
                    timestamp = currentTimestamp
                )

                historyRepository.record(info)
                currentTimestamp += interval
            }
        }
    }
}
