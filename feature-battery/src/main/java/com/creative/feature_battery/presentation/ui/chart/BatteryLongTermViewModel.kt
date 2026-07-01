package com.creative.feature_battery.presentation.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.repository.BatteryAggregation
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LongTermWindow(val days: Int) {
    WEEK(7),
    MONTH(30),
    ALL(-1)
}

data class LongTermChartUiState(
    val aggregations: List<BatteryAggregation> = emptyList(),
    val window: LongTermWindow = LongTermWindow.WEEK,
    val startTimestamp: Long = 0L,
    val endTimestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryLongTermViewModel(
    private val historyRepository: BatteryHistoryRepository
) : ViewModel() {

    private val _selectedWindow = MutableStateFlow(LongTermWindow.WEEK)
    val selectedWindow: StateFlow<LongTermWindow> = _selectedWindow

    val avgLevelModelProducer = CartesianChartModelProducer()
    val avgTempModelProducer = CartesianChartModelProducer()
    val avgVoltageModelProducer = CartesianChartModelProducer()
    val avgCurrentModelProducer = CartesianChartModelProducer()

    val uiState: StateFlow<LongTermChartUiState> = _selectedWindow.flatMapLatest { window ->
        val now = System.currentTimeMillis()
        val since = if (window.days > 0) {
            now - (window.days.toLong() * 24 * 60 * 60 * 1000)
        } else {
            0L
        }
        historyRepository.observeAggregatedHistory(since).map { list ->
            LongTermChartUiState(
                aggregations = list.sortedBy { it.timestamp },
                window = window,
                startTimestamp = since,
                endTimestamp = now
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LongTermChartUiState()
    )

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.aggregations.size < 2) return@collect

                avgLevelModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.aggregations.map { it.timestamp.toDouble() },
                            state.aggregations.map { it.avgLevel.toDouble() }
                        )
                    }
                }
                avgTempModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.aggregations.map { it.timestamp.toDouble() },
                            state.aggregations.map { it.avgTemperatureC.toDouble() }
                        )
                    }
                }
                avgVoltageModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.aggregations.map { it.timestamp.toDouble() },
                            state.aggregations.map { it.avgVoltageMv?.toDouble() ?: 0.0 }
                        )
                    }
                }
                avgCurrentModelProducer.runTransaction {
                    lineSeries {
                        series(
                            state.aggregations.map { it.timestamp.toDouble() },
                            state.aggregations.map { it.avgCurrentMa?.toDouble() ?: 0.0 }
                        )
                    }
                }
            }
        }
    }

    fun setWindow(window: LongTermWindow) {
        _selectedWindow.value = window
    }
}
