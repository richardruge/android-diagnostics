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
    private val _isPingTesting = MutableStateFlow(false)

    val uiState: StateFlow<NetworkUiState> = combine(
        repository.observeNetworkState(),
        _pingState,
        _isPingTesting
    ) { state, ping, testing ->
        NetworkUiState(
            networkState = state,
            lastPingMs = ping,
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
            _isPingTesting.value = false
        }
    }
}
