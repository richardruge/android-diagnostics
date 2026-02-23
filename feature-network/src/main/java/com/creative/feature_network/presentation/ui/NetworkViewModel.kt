package com.creative.feature_network.presentation.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_network.domain.usecases.GetNetworkStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NetworkViewModel(
    private val getNetworkState: GetNetworkStateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkUiState())
    val state: StateFlow<NetworkUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = NetworkUiState(isLoading = true)

            try {
                val result = getNetworkState()
                _state.value = NetworkUiState(
                    levelPercent = result.levelPercent,
                    health = result.health,
                    isCharging = result.isCharging,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = NetworkUiState(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }
}