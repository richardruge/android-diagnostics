package com.creative.feature_automation.presentation.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_automation.domain.usecases.RunAutomationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AutomationViewModel(
    private val runAutomation: RunAutomationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AutomationUiState())
    val state: StateFlow<AutomationUiState> = _state

    fun execute(taskId: String) {
        viewModelScope.launch {
            _state.value = AutomationUiState(isRunning = true)

            try {
                val result = runAutomation(taskId)
                _state.value = AutomationUiState(
                    resultMessage = result.message,
                    isRunning = false
                )
            } catch (e: Exception) {
                _state.value = AutomationUiState(
                    errorMessage = e.message,
                    isRunning = false
                )
            }
        }
    }
}