package com.creative.feature_battery.domain

import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.Flow

class GetBatteryInfoUseCase(
    private val repo: BatteryRepository
) {
    operator fun invoke(): Flow<BatteryInfo> = repo.observeBatteryInfo()
}