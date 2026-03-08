package com.creative.feature_battery.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.creative.feature_battery.data.history.BatteryHistoryEntity

@Dao
interface BatteryHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BatteryHistoryEntity)

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
}