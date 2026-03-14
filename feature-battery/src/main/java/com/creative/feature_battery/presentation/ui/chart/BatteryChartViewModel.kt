package com.creative.feature_battery.presentation.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_data.thermal.ThermalRepository
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class BatteryChartViewModel(
    historyRepository: BatteryHistoryRepository,
    private val thermalRepository: ThermalRepository
) : ViewModel() {

    val chartData = historyRepository.observeHistory()
        .map { full ->
            val cutoff = System.currentTimeMillis() - 60 * 60 * 1000
            full.filter { it.timestamp >= cutoff }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val thermalStatus = flow {
        while (true) {
            emit(thermalRepository.getThermalStatus())
            delay(5000)
        }
    }.onStart {
        emit(thermalRepository.getThermalStatus())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
}
