package com.creative.feature_battery.data.history

import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.BatteryHealthUi
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import kotlinx.coroutines.flow.map

class BatteryHistoryRepositoryImpl(
    private val dao: BatteryHistoryDao,
    private val maxSize: Int = 10_000
) : BatteryHistoryRepository {

    override suspend fun record(info: BatteryInfo) {
        dao.insert(info.toEntity())
        dao.trimToSize(maxSize)
    }

    override fun observeHistory() =
        dao.observeHistory().map { list -> list.map { it.toDomain() } }
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
    timestamp = timestamp
)

fun BatteryHistoryEntity.toDomain() = BatteryInfo(
    level = level,
    temperatureC = temperatureC,
    isCharging = isCharging,
    chargeRateMah = chargeRateMah,
    health = BatteryHealthUi.valueOf(health),
    capacityMah = capacityMah,
    voltageMv = voltageMv,
    technology = technology,
    timestamp = timestamp
)