package com.creative.feature_battery.presentation

import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.ChargingRate
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
        val technology: String?,
        val cycleCount: Int?,
        val stateOfHealth: Int?,
        val currentNowMa: Int?,
        val currentAverageMa: Int?,
        val chargingRate: ChargingRate
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
                technology = info.technology,
                cycleCount = info.cycleCount,
                stateOfHealth = info.stateOfHealth,
                currentNowMa = info.currentNowMa,
                currentAverageMa = info.currentAverageMa,
                chargingRate = info.chargingRate
            )
        }
    }
}
