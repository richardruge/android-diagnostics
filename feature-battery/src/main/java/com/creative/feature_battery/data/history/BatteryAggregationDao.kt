package com.creative.feature_battery.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryAggregationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(aggregation: BatteryAggregationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(aggregations: List<BatteryAggregationEntity>)

    @Query("SELECT * FROM battery_aggregation WHERE timestamp > :since ORDER BY timestamp DESC")
    fun observeAggregations(since: Long): Flow<List<BatteryAggregationEntity>>

    @Query("SELECT * FROM battery_aggregation ORDER BY timestamp DESC")
    fun getAllAggregations(): List<BatteryAggregationEntity>

    @Query("DELETE FROM battery_aggregation WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM battery_aggregation")
    suspend fun deleteAll()
}
