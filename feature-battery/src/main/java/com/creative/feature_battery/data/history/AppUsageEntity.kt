package com.creative.feature_battery.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.creative.core_model.ForegroundSession

@Entity(tableName = "app_usage_history")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val avgMa: Double,
    val totalMah: Double
)

fun ForegroundSession.toEntity() = AppUsageEntity(
    packageName = packageName,
    startTime = startTime,
    endTime = endTime,
    avgMa = avgMa,
    totalMah = totalMah
)

fun AppUsageEntity.toDomain() = ForegroundSession(
    packageName = packageName,
    startTime = startTime,
    endTime = endTime,
    avgMa = avgMa,
    totalMah = totalMah
)
