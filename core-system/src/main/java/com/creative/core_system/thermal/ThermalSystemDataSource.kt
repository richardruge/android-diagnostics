package com.creative.core_system.thermal

import kotlinx.coroutines.flow.Flow

interface ThermalSystemDataSource {
    fun thermalStatus(): Flow<Int>
}
