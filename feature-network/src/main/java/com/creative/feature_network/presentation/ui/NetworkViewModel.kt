package com.creative.feature_network.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_data.network.NetworkRepository
import com.creative.core_model.NetworkState
import com.creative.core_model.NetworkType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NetworkViewModel(
    private val repository: NetworkRepository
) : ViewModel() {

    private val _pingState = MutableStateFlow<Long?>(null)
    private val _pingHistory = MutableStateFlow<List<Long>>(emptyList())
    private val _isPingTesting = MutableStateFlow(false)

    val uiState: StateFlow<NetworkUiState> = combine(
        repository.observeNetworkState(),
        _pingState,
        _pingHistory,
        _isPingTesting
    ) { state, ping, history, testing ->
        NetworkUiState(
            networkState = state,
            lastPingMs = ping,
            pingHistory = history,
            isPingTesting = testing,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkUiState(isLoading = true)
    )

    fun runPingTest() {
        viewModelScope.launch {
            _isPingTesting.value = true
            val result = repository.runPingTest()
            _pingState.value = result
            if (result != null) {
                _pingHistory.value = (_pingHistory.value + result).takeLast(20)
            }
            _isPingTesting.value = false
        }
    }
}
