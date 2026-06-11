package com.creative.feature_network.presentation.ui

import com.creative.core_model.NetworkState

data class NetworkUiState(
    val networkState: NetworkState? = null,
    val isLoading: Boolean = true,
    val isPingTesting: Boolean = false,
    val lastPingMs: Long? = null,
    val pingHistory: List<Long> = emptyList(),
    val errorMessage: String? = null
)
