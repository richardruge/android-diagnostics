package com.creative.feature_automation.domain.usecases

import com.creative.core_data.automation.AutomationRepository

class RunAutomationUseCase(
    private val repo: AutomationRepository
) {
    suspend operator fun invoke(taskId: String) = repo.runAutomation()
}