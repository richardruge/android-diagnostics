package com.creative.feature_battery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.repository.BatterySettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BatterySettingsUiState(
    val retentionMonths: Int = 6,
    val estimatedStorageKb: Double = 0.0
)

class BatterySettingsViewModel(
    private val settingsRepository: BatterySettingsRepository
) : ViewModel() {

    val uiState: StateFlow<BatterySettingsUiState> = settingsRepository.getSettings()
        .map { settings ->
            BatterySettingsUiState(
                retentionMonths = settings.retentionMonths,
                estimatedStorageKb = calculateStorageEstimate(settings.retentionMonths)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BatterySettingsUiState()
        )

    fun updateRetentionPeriod(months: Int) {
        viewModelScope.launch {
            settingsRepository.updateRetentionPeriod(months)
        }
    }

    private fun calculateStorageEstimate(months: Int): Double {
        // 1 hour buckets = 24 entries per day = 720 per month
        // Estimated row size ~150 bytes (including SQLite overhead/indexes)
        val entriesPerMonth = 24 * 30
        val bytesPerRow = 150 
        return (months * entriesPerMonth * bytesPerRow) / 1024.0
    }
}
