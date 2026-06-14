package com.creative.feature_network.presentation.ui

import com.creative.core_model.NetworkState

data class NetworkUiState(
    val networkState: NetworkState? = null,
    val isLoading: Boolean = true,
    val isPingTesting: Boolean = false,
    val gatewayPingMs: Long? = null,
    val dnsPingMs: Long? = null,
    val publicPingMs: Long? = null,
    val pingHistory: List<Long> = emptyList(),
    val signalHistory: List<Int> = emptyList(),
    val errorMessage: String? = null
)
