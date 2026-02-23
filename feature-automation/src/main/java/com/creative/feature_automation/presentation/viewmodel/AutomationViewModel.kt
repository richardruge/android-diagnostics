package com.creative.feature_automation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_model.AutomationResult
import com.creative.feature_automation.domain.usecases.RunAutomationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AutomationViewModel(
    private val runAutomation: RunAutomationUseCase
) : ViewModel() {

    private val _result = MutableStateFlow<AutomationResult?>(null)
    val result = _result.asStateFlow()

    fun execute(taskId: String) {
        viewModelScope.launch {
            _result.value = runAutomation(taskId)
        }
    }
}