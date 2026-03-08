package com.creative.feature_battery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.BatterySeverityEvaluator
import com.creative.feature_battery.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BatteryViewModel(
    private val repository: BatteryRepository,
    private val evaluator: BatterySeverityEvaluator
) : ViewModel() {

    private val _uiState = MutableStateFlow<BatteryUiState>(BatteryUiState.Loading)
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    init {
        observeBattery()
    }

    private fun observeBattery() {
        viewModelScope.launch {
            repository.observeBatteryInfo()
                .map { info ->
                    val severity = evaluator.evaluate(info)
                    BatteryUiState.from(info, severity)
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }
}