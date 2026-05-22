package com.creative.feature_battery.data.history

import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.BatteryHealthUi
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BatteryHistoryRepositoryImpl(
    private val dao: BatteryHistoryDao,
    private val maxSize: Int = 10_000
) : BatteryHistoryRepository {

    override suspend fun record(info: BatteryInfo) {
        dao.insert(info.toEntity())
        dao.trimToSize(maxSize)
    }

    override fun observeHistory(): Flow<List<BatteryInfo>> =
        dao.observeHistory().map { list -> list.map { it.toDomain() } }

    override fun observeHistory(since: Long): Flow<List<BatteryInfo>> =
        dao.observeRecentHistory(since).map { list -> list.map { it.toDomain() } }
}

// Mapping extensions
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
    } catch (e: Exception) {
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
