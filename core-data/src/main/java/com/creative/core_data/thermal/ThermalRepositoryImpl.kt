package com.creative.core_data.thermal

import com.creative.core_model.ThermalSeverity
import com.creative.core_model.ThermalStatus
import com.creative.core_system.thermal.ThermalSystemDataSource

class ThermalRepositoryImpl(
    private val system: ThermalSystemDataSource
) : ThermalRepository {

    override suspend fun getThermalStatus(): ThermalStatus {
        // Phase 0 placeholder
        return ThermalStatus(
            status = "Success",
            temperatureC = 0f,
            severity = ThermalSeverity.NORMAL
        )
    }
}