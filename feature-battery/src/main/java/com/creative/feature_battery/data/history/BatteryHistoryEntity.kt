package com.creative.feature_battery.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_history")
data class BatteryHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val level: Int,
    val temperatureC: Float,
    val isCharging: Boolean,
    val chargeRateMah: Int?,
    val health: String,
    val capacityMah: Int?,
    val designCapacityMah: Int?,
    val voltageMv: Int?,
    val technology: String?,
    val cycleCount: Int?,
    val stateOfHealth: Int?,
    val currentNowMa: Int?,
    val currentAverageMa: Int?,
    val maxChargingCurrentUa: Int?,
    val maxChargingVoltageMv: Int?,
    val timestamp: Long
)
