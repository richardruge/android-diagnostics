package com.creative.feature_automation.presentation.ui

data class AutomationUiState(
    val resultMessage: String? = null,
    val isRunning: Boolean = false,
    val errorMessage: String? = null
)