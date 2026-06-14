package com.creative.feature_battery.data

import android.content.Context
import android.content.SharedPreferences
import com.creative.feature_battery.domain.model.BatterySettings
import com.creative.feature_battery.domain.repository.BatterySettingsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import androidx.core.content.edit

class BatterySettingsRepositoryImpl(
    private val context: Context
) : BatterySettingsRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("battery_settings", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_RETENTION_MONTHS = "retention_months"
        private const val DEFAULT_RETENTION_MONTHS = 6
    }

    override fun getSettings(): Flow<BatterySettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_RETENTION_MONTHS) {
                trySend(BatterySettings(p.getInt(KEY_RETENTION_MONTHS, DEFAULT_RETENTION_MONTHS)))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(BatterySettings(prefs.getInt(KEY_RETENTION_MONTHS, DEFAULT_RETENTION_MONTHS)))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun updateRetentionPeriod(months: Int) {
        prefs.edit { putInt(KEY_RETENTION_MONTHS, months) }
    }
}
