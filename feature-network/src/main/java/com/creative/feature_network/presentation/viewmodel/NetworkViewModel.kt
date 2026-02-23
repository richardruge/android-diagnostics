package com.creative.feature_network.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_model.NetworkState
import com.creative.feature_network.domain.usecases.GetNetworkStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkViewModel(
    private val getNetworkState: GetNetworkStateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<NetworkState?>(null)
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = getNetworkState()
        }
    }
}