package com.creative.feature_battery.data.history

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BatteryHistoryEntity::class,
        BatteryAggregationEntity::class,
        AppUsageEntity::class
    ],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 7, to = 8)
    ],
    exportSchema = true
)
abstract class BatteryHistoryDatabase : RoomDatabase() {
    abstract fun dao(): BatteryHistoryDao
    abstract fun aggregationDao(): BatteryAggregationDao
    abstract fun usageDao(): AppUsageDao
}
