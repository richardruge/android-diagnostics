package com.creative.feature_battery.domain.model

enum class ChargingRate {
    NONE,
    SLOW,      // < 5W
    NORMAL,    // 5W - 15W
    FAST,      // 15W - 30W
    ULTRA_FAST // > 30W
}
