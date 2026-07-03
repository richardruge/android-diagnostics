package com.creative.feature_battery.data.history

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BatteryHistoryEntity::class,
        BatteryAggregationEntity::class,
        AppUsageEntity::class,
    ],
    version = 9,
    exportSchema = true
)
abstract class BatteryHistoryDatabase : RoomDatabase() {
    abstract fun dao(): BatteryHistoryDao
    abstract fun aggregationDao(): BatteryAggregationDao
    abstract fun usageDao(): AppUsageDao
}
