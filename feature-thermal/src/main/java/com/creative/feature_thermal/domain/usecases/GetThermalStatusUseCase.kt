package com.creative.feature_thermal.domain.usecases

import com.creative.core_data.thermal.ThermalRepository

class GetThermalStatusUseCase(
    private val repo: ThermalRepository
) {
    suspend operator fun invoke() = repo.getThermalStatus()
}