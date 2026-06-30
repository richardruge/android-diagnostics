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
        private const val KEY_IGNORE_SYSTEM = "ignore_system_processes"
        private const val DEFAULT_IGNORE_SYSTEM = true
    }

    override fun getSettings(): Flow<BatterySettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == KEY_RETENTION_MONTHS || key == KEY_IGNORE_SYSTEM) {
                trySend(readSettings(p))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(readSettings(prefs))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun readSettings(p: SharedPreferences): BatterySettings {
        return BatterySettings(
            retentionMonths = p.getInt(KEY_RETENTION_MONTHS, DEFAULT_RETENTION_MONTHS),
            ignoreSystemProcesses = p.getBoolean(KEY_IGNORE_SYSTEM, DEFAULT_IGNORE_SYSTEM)
        )
    }

    override suspend fun updateRetentionPeriod(months: Int) {
        prefs.edit { putInt(KEY_RETENTION_MONTHS, months) }
    }

    override suspend fun updateIgnoreSystemProcesses(ignore: Boolean) {
        prefs.edit { putBoolean(KEY_IGNORE_SYSTEM, ignore) }
    }
}
