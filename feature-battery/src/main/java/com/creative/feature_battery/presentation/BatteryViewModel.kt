package com.creative.feature_battery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.BatterySeverityEvaluator
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.creative.feature_battery.usage.ForegroundSessionManager
import com.creative.feature_battery.usage.UsagePermissionHelper
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BatteryViewModel(
    private val repository: BatteryRepository,
    private val evaluator: BatterySeverityEvaluator,
    private val sessionManager: ForegroundSessionManager,
    private val permissionHelper: UsagePermissionHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<BatteryUiState>(BatteryUiState.Loading)
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    init {
        observeBattery()
    }

    private fun observeBattery() {
        viewModelScope.launch {
            combine(
                repository.observeBatteryInfo(),
                sessionManager.foregroundAppFlow
            ) { info, foregroundApp ->
                val severity = evaluator.evaluate(info)
                val hasPermission = permissionHelper.checkPermission()
                BatteryUiState.from(info, severity, foregroundApp, hasPermission)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun requestUsagePermission(context: Context) {
        permissionHelper.requestPermission(context)
    }
}
