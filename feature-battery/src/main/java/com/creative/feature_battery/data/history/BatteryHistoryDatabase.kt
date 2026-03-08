package com.creative.feature_battery.data.history

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BatteryHistoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class BatteryHistoryDatabase : RoomDatabase() {
    abstract fun dao(): BatteryHistoryDao
}