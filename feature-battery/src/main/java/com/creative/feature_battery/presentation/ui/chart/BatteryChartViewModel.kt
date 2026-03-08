package com.creative.feature_battery.presentation.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BatteryChartViewModel(
    historyRepository: BatteryHistoryRepository
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
}