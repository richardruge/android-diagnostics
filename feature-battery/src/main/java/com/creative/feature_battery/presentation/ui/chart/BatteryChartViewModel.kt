package com.creative.feature_battery.presentation.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_data.thermal.ThermalRepository
import com.creative.core_model.ThermalSeverity
import com.creative.feature_battery.domain.BatterySeverityEvaluator
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

enum class TimeWindow(val minutes: Long) {
    MIN_15(15),
    HOUR_1(60),
    HOUR_24(1440)
}

data class ChartUiState(
    val data: List<BatteryInfo> = emptyList(),
    val window: TimeWindow = TimeWindow.HOUR_1
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
    historyRepository: BatteryHistoryRepository,
    private val batteryRepository: BatteryRepository,
    private val thermalRepository: ThermalRepository,
    private val evaluator: BatterySeverityEvaluator
) : ViewModel() {

    private val _selectedWindow = MutableStateFlow(TimeWindow.HOUR_1)
    val selectedWindow: StateFlow<TimeWindow> = _selectedWindow

    val batteryLevelModelProducer = CartesianChartModelProducer()
    val temperatureModelProducer = CartesianChartModelProducer()

    val chartUiState: StateFlow<ChartUiState> = combine(
        historyRepository.observeHistory(),
        _selectedWindow
    ) { history, window ->
        val cutoff = System.currentTimeMillis() - (window.minutes * 60 * 1000)
        val filtered = history.filter { 
            it.timestamp >= cutoff && it.temperatureC.isFinite()
        }
            .sortedBy { it.timestamp }
            .distinctBy { it.timestamp }
        ChartUiState(filtered, window)
    }
    .onEach { state ->
        if (state.data.isNotEmpty()) {
            batteryLevelModelProducer.runTransaction {
                lineSeries {
                    series(
                        // Use raw timestamp (milliseconds) as X value to avoid precision issues with Double division.
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
        } else {
            // Clear producers if there is no data
            batteryLevelModelProducer.runTransaction { lineSeries { } }
            temperatureModelProducer.runTransaction { lineSeries { } }
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
}
