package com.creative.core_data.automation

import com.creative.core_data.automation.AutomationRepository
import com.creative.core_model.AutomationHealth
import com.creative.core_model.AutomationResult
import com.creative.core_system.automation.AutomationSystemDataSource

class AutomationRepositoryImpl(
    private val system: AutomationSystemDataSource
) : AutomationRepository {

    suspend fun getAutomationStatus(): AutomationResult {
        // Phase 0 placeholder
        return AutomationResult(
            success = true,
            message = "Success",
            levelPercent = 0f,
            health = AutomationHealth.UNKNOWN,
            isCharging = false
        )
    }

    override suspend fun runAutomation(): AutomationResult {
        TODO("Not yet implemented")
    }
}