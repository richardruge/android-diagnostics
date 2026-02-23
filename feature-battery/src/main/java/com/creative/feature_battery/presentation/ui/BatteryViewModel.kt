package com.creative.feature_battery.presentation.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.BatteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BatteryViewModel(
    private val repository: BatteryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BatteryUiState())
    val state: StateFlow<BatteryUiState> = _state

    init {
        viewModelScope.launch {
            repository.batteryTemperatureC().collect { temp ->
                _state.value = BatteryUiState(
                    temperatureC = temp,
                    loading = false
                )
            }
        }
    }
}