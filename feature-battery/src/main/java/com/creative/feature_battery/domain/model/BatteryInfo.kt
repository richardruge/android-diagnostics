package com.creative.feature_battery.domain.model

data class BatteryInfo(
    val level: Int,
    val temperatureC: Float,
    val isCharging: Boolean,
    val chargeRateMah: Int?,
    val health: BatteryHealthUi,
    val capacityMah: Int?,
    val voltageMv: Int?,
    val technology: String?,
    val cycleCount: Int?,
    val stateOfHealth: Int?,
    val currentNowMa: Int?,
    val currentAverageMa: Int?,
    val maxChargingCurrentUa: Int?,
    val maxChargingVoltageMv: Int?,
    val timestamp: Long
) {
    /**
     * Normalizes voltage to Volts. 
     * Handles cases where the system reports in microvolts (e.g. 4,000,000) instead of millivolts (4,000).
     */
    private fun normalizeVoltage(mvOrUv: Int?): Double? {
        if (mvOrUv == null || mvOrUv <= 0) return null
        val value = mvOrUv.toDouble()
        // If voltage > 100,000, it's almost certainly microvolts (e.g., 4,200,000uV = 4.2V)
        // rather than millivolts (4,200mV = 4.2V). 100V is an impossible battery voltage for a phone.
        return if (value > 100_000) value / 1_000_000.0 else value / 1_000.0
    }

    /**
     * Normalizes current to Amperes.
     */
    private fun normalizeCurrent(maOrUa: Int?, isMicro: Boolean = false): Double? {
        if (maOrUa == null) return null
        val value = maOrUa.toDouble()
        return if (isMicro || kotlin.math.abs(value) > 100_000) {
            value / 1_000_000.0
        } else {
            value / 1_000.0
        }
    }

    val powerW: Double?
        get() {
            val vV = normalizeVoltage(voltageMv) ?: return null
            val cA = normalizeCurrent(currentNowMa) ?: return null
            return kotlin.math.abs(vV * cA)
        }

    val maxPowerW: Double?
        get() {
            val vV = normalizeVoltage(maxChargingVoltageMv) ?: return null
            val cA = normalizeCurrent(maxChargingCurrentUa, isMicro = true) ?: return null
            return vV * cA
        }

    val chargingRate: ChargingRate
        get() {
            if (!isCharging) return ChargingRate.NONE
            val pW = powerW ?: return ChargingRate.NONE
            
            return when {
                pW < 5.0 -> ChargingRate.SLOW
                pW < 15.0 -> ChargingRate.NORMAL
                pW < 30.0 -> ChargingRate.FAST
                else -> ChargingRate.ULTRA_FAST
            }
        }
}
