package com.creative.feature_battery.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AppUsageEntity)

    @Query("SELECT * FROM app_usage_history ORDER BY startTime DESC")
    fun observeAllUsage(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage_history ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentUsage(limit: Int): List<AppUsageEntity>

    @Query("DELETE FROM app_usage_history WHERE endTime < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)

    @Query("DELETE FROM app_usage_history")
    suspend fun deleteAll()
}
