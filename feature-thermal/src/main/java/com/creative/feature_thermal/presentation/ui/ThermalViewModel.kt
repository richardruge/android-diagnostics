package com.creative.feature_thermal.presentation.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_thermal.domain.usecases.GetThermalStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThermalViewModel(
    private val getThermalStatus: GetThermalStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ThermalUiState())
    val state: StateFlow<ThermalUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = ThermalUiState(isLoading = true)

            try {
                val result = getThermalStatus()
                _state.value = ThermalUiState(
                    status = result.status,
                    temperatureC = result.temperatureC,
                    severity = result.severity,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = ThermalUiState(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }
}