package com.creative.feature_battery.data.history

import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.BatteryHealthUi
import com.creative.feature_battery.domain.repository.BatteryAggregation
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatterySettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BatteryHistoryRepositoryImpl(
    private val dao: BatteryHistoryDao,
    private val aggregationDao: BatteryAggregationDao,
    private val settingsRepository: BatterySettingsRepository,
    private val maxSize: Int = 10_000
) : BatteryHistoryRepository {

    private var lastAggregationTime = 0L

    override suspend fun record(info: BatteryInfo) {
        dao.insert(info.toEntity())
        
        val now = System.currentTimeMillis()
        // Aggregating every 30 mins keeps the data clean without too many writes
        if (now - lastAggregationTime > 30 * 60 * 1000) {
            runAggregation()
            cleanupOldAggregations()
            lastAggregationTime = now
        }
        
        dao.trimToSize(maxSize)
    }

    private suspend fun cleanupOldAggregations() {
        val settings = settingsRepository.getSettings().first()
        val retentionMillis = settings.retentionMonths * 30L * 24 * 60 * 60 * 1000
        val cutoff = System.currentTimeMillis() - retentionMillis
        aggregationDao.deleteOlderThan(cutoff)
    }

    override suspend fun runAggregation() {
        val now = System.currentTimeMillis()
        val aggregationCutoff = now - (1 * 60 * 60 * 1000) // Aggregate data older than 1 hour
        val deletionCutoff = now - (24 * 60 * 60 * 1000) // Keep 24 hours of raw data for the main charts
        val bucketMinutes = 60 // 1 hour buckets
        
        val buckets = dao.getAggregatedHistory(aggregationCutoff, bucketMinutes)
        if (buckets.isNotEmpty()) {
            val aggregations = buckets.map { bucket ->
                BatteryAggregationEntity(
                    timestamp = bucket.timestamp,
                    avgLevel = bucket.avgLevel,
                    avgTemperatureC = bucket.avgTemperatureC,
                    avgVoltageMv = bucket.avgVoltageMv,
                    avgCurrentMa = bucket.avgCurrentMa,
                    bucketDurationMinutes = bucketMinutes
                )
            }
            aggregationDao.insertAll(aggregations)
            dao.deleteOlderThan(deletionCutoff)
        }
    }

    override fun observeHistory(): Flow<List<BatteryInfo>> =
        dao.observeHistory().map { list -> list.map { it.toDomain() } }

    override fun observeHistory(since: Long, limit: Int): Flow<List<BatteryInfo>> =
        dao.observeRecentHistory(since, limit).map { list -> list.map { it.toDomain() } }

    override fun observeHistorySampled(since: Long, samplingRate: Int, limit: Int): Flow<List<BatteryInfo>> =
        dao.observeSampledHistory(since, samplingRate, limit).map { list -> list.map { it.toDomain() } }

    override fun observeAggregatedHistory(since: Long): Flow<List<BatteryAggregation>> =
        aggregationDao.observeAggregations(since).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun clearHistory() {
        dao.deleteAll()
        aggregationDao.deleteAll()
    }
}

// Mapping extensions
fun BatteryAggregationEntity.toDomain() = BatteryAggregation(
    timestamp = timestamp,
    avgLevel = avgLevel,
    avgTemperatureC = avgTemperatureC,
    avgVoltageMv = avgVoltageMv,
    avgCurrentMa = avgCurrentMa,
    bucketDurationMinutes = bucketDurationMinutes
)
fun BatteryInfo.toEntity() = BatteryHistoryEntity(
    level = level,
    temperatureC = temperatureC,
    isCharging = isCharging,
    chargeRateMah = chargeRateMah,
    health = health.name,
    capacityMah = capacityMah,
    voltageMv = voltageMv,
    technology = technology,
    cycleCount = cycleCount,
    stateOfHealth = stateOfHealth,
    currentNowMa = currentNowMa,
    currentAverageMa = currentAverageMa,
    maxChargingCurrentUa = maxChargingCurrentUa,
    maxChargingVoltageMv = maxChargingVoltageMv,
    timestamp = timestamp
)

fun BatteryHistoryEntity.toDomain() = BatteryInfo(
    level = level,
    temperatureC = temperatureC,
    isCharging = isCharging,
    chargeRateMah = chargeRateMah,
    health = try {
        BatteryHealthUi.valueOf(health)
    } catch (_: Exception) {
        BatteryHealthUi.UNSPECIFIED
    },
    capacityMah = capacityMah,
    voltageMv = voltageMv,
    technology = technology,
    cycleCount = cycleCount,
    stateOfHealth = stateOfHealth,
    currentNowMa = currentNowMa,
    currentAverageMa = currentAverageMa,
    maxChargingCurrentUa = maxChargingCurrentUa,
    maxChargingVoltageMv = maxChargingVoltageMv,
    timestamp = timestamp
)
