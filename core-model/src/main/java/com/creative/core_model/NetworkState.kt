package com.creative.core_model

data class NetworkState(
    val levelPercent: Int,
    val health: NetworkHealth,
    val isCharging: Boolean
)

enum class NetworkHealth {
    GOOD, FAIR, POOR, UNKNOWN
}
