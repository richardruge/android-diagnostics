package com.creative.feature_battery.domain.usecases

import com.creative.core_data.battery.BatteryRepository

class GetBatteryStatusUseCase(
    private val repo: BatteryRepository
) {
    suspend operator fun invoke() = repo.getBatteryStatus()
}