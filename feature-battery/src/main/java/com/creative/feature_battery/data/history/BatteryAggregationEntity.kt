package com.creative.feature_battery.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_aggregation")
data class BatteryAggregationEntity(
    @PrimaryKey val timestamp: Long, // Start of the bucket
    val avgLevel: Float,
    val avgTemperatureC: Float,
    val avgVoltageMv: Float?,
    val avgCurrentMa: Float?,
    val bucketDurationMinutes: Int
)
