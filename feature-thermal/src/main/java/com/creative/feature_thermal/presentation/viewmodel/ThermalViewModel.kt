package com.creative.feature_thermal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_model.ThermalStatus
import com.creative.feature_thermal.domain.usecases.GetThermalStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThermalViewModel(
    private val getThermalStatus: GetThermalStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ThermalStatus?>(null)
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = getThermalStatus()
        }
    }
}