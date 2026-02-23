package com.creative.feature_network.presentation.ui

import com.creative.core_model.NetworkHealth

data class NetworkUiState(
    val levelPercent: Int? = null,
    val health: NetworkHealth? = null,
    val isCharging: Boolean? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)