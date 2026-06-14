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
    val window: LongTermWindow = LongTermWindow.WEEK
)

@OptIn(ExperimentalCoroutinesApi::class)
class BatteryLongTermViewModel(
    private val historyRepository: BatteryHistoryRepository
) : ViewModel() {

    private val _selectedWindow = MutableStateFlow(LongTermWindow.WEEK)
    val selectedWindow: StateFlow<LongTermWindow> = _selectedWindow

    val avgLevelModelProducer = CartesianChartModelProducer()
    val avgTempModelProducer = CartesianChartModelProducer()

    val uiState: StateFlow<LongTermChartUiState> = _selectedWindow.flatMapLatest { window ->
        val since = if (window.days > 0) {
            System.currentTimeMillis() - (window.days.toLong() * 24 * 60 * 60 * 1000)
        } else {
            0L
        }
        historyRepository.observeAggregatedHistory(since).map { list ->
            LongTermChartUiState(list.sortedBy { it.timestamp }, window)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LongTermChartUiState()
    )

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                if (state.aggregations.size >= 2) {
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
                }
            }
        }
    }

    fun setWindow(window: LongTermWindow) {
        _selectedWindow.value = window
    }
}
