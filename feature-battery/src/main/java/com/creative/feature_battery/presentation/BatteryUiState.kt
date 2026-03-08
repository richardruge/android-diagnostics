package com.creative.feature_battery.presentation

import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.Severity

sealed interface BatteryUiState {
    data object Loading : BatteryUiState

    data class Ready(
        val level: Int,
        val temperatureC: Float,
        val isCharging: Boolean,
        val health: String,
        val severity: Severity,
        val capacityMah: Int?,
        val voltageMv: Int?,
        val technology: String?
    ) : BatteryUiState

    companion object {
        fun from(info: BatteryInfo, severity: Severity): BatteryUiState {
            return Ready(
                level = info.level,
                temperatureC = info.temperatureC,
                isCharging = info.isCharging,
                health = info.health.name,
                severity = severity,
                capacityMah = info.capacityMah,
                voltageMv = info.voltageMv,
                technology = info.technology
            )
        }
    }
}