package com.creative.core_data.automation

import com.creative.core_model.AutomationResult

interface AutomationRepository {
    suspend fun runAutomation(): AutomationResult
}