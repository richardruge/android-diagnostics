package com.creative.core_model

data class NetworkState(
    val isConnected: Boolean,
    val type: NetworkType,
    val signalStrengthDbm: Int?,
    val signalLevel: Int, // 0 to 4
    val ssid: String? = null,
    val frequencyMhz: Int? = null,
    val wifiStandard: String? = null,
    val linkSpeedMbps: Int? = null,
    val ipAddress: String? = null
)

enum class NetworkType {
    WIFI, CELLULAR, ETHERNET, NONE, UNKNOWN
}

enum class NetworkHealth {
    EXCELLENT, GOOD, FAIR, POOR, NONE
}
