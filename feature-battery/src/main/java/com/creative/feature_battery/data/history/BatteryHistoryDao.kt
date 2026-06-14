package com.creative.feature_battery.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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
