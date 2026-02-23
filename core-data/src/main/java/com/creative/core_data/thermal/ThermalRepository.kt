package com.creative.core_data.thermal

import com.creative.core_model.ThermalStatus

interface ThermalRepository {
    suspend fun getThermalStatus(): ThermalStatus
}