package com.creative.core_system.automation

interface AutomationSystemDataSource {
    suspend fun runAutomationTask(taskId: String): Boolean
}