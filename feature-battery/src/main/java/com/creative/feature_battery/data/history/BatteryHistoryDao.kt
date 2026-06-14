package com.creative.feature_battery.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class BatteryHistoryBucket(
    @ColumnInfo(name = "bucket_timestamp") val timestamp: Long,
    @ColumnInfo(name = "avg_level") val avgLevel: Float,
    @ColumnInfo(name = "avg_temp") val avgTemperatureC: Float,
    @ColumnInfo(name = "avg_voltage") val avgVoltageMv: Float?,
    @ColumnInfo(name = "avg_current") val avgCurrentMa: Float?
)

@Dao
interface BatteryHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BatteryHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BatteryHistoryEntity>)

    @Query("SELECT * FROM battery_history WHERE timestamp > :since ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentHistory(since: Long, limit: Int = 2000): Flow<List<BatteryHistoryEntity>>

    @Query("""
        SELECT * FROM (
            SELECT *, row_number() OVER (ORDER BY timestamp DESC) as row_num 
            FROM battery_history 
            WHERE timestamp > :since
        ) 
        WHERE row_num % :samplingRate = 0 
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun observeSampledHistory(since: Long, samplingRate: Int, limit: Int = 2000): Flow<List<BatteryHistoryEntity>>

    @Query("SELECT * FROM battery_history ORDER BY timestamp DESC")
    fun observeHistory(): Flow<List<BatteryHistoryEntity>>

    @Query("""
        SELECT 
            (timestamp / (60000 * :bucketMinutes)) * (60000 * :bucketMinutes) as bucket_timestamp,
            AVG(level) as avg_level,
            AVG(temperatureC) as avg_temp,
            AVG(voltageMv) as avg_voltage,
            AVG(currentNowMa) as avg_current
        FROM battery_history 
        WHERE timestamp < :olderThan
        GROUP BY bucket_timestamp
        ORDER BY bucket_timestamp ASC
    """)
    suspend fun getAggregatedHistory(olderThan: Long, bucketMinutes: Int): List<BatteryHistoryBucket>

    @Query("DELETE FROM battery_history WHERE timestamp < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long)

    @Query("""
        DELETE FROM battery_history 
        WHERE id NOT IN (
            SELECT id FROM battery_history 
            ORDER BY timestamp DESC 
            LIMIT :maxSize
        )
    """)
    suspend fun trimToSize(maxSize: Int)

    @Query("DELETE FROM battery_history")
    suspend fun deleteAll()
}
