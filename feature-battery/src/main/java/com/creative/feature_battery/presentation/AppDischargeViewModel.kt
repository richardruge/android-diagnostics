package com.creative.feature_battery.presentation

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.creative.feature_battery.domain.repository.BatterySettingsRepository
import com.creative.feature_battery.usage.ForegroundSessionManager
import com.creative.feature_battery.usage.PackageUtils
import com.creative.feature_battery.usage.UsagePermissionHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

data class AppDischargeUiState(
    val sessions: List<AppDischargeUiModel> = emptyList(),
    val maxMah: Double = 1.0,
    val totalTrackedMah: Double = 0.0,
    val selectedWindow: AppTimeWindow = AppTimeWindow.HOUR_1,
    val hasPermission: Boolean = true
)

enum class AppTimeWindow(val label: String, val durationMs: Long) {
    HOUR_1("1h", 60 * 60 * 1000L),
    HOUR_6("6h", 6 * 60 * 60 * 1000L),
    HOUR_24("24h", 24 * 60 * 60 * 1000L)
}

data class AppDischargeUiModel(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val totalMah: Double,
    val totalPercentage: Double,
    val drainRatePercentPerHour: Double,
    val durationMs: Long,
    val sparklinePoints: List<Float> = emptyList()
)

class AppDischargeViewModel(
    private val sessionManager: ForegroundSessionManager,
    private val batteryRepository: BatteryRepository,
    private val settingsRepository: BatterySettingsRepository,
    private val permissionHelper: UsagePermissionHelper,
    private val context: Context
) : ViewModel() {

    private val packageManager = context.packageManager
    private val _selectedWindow = MutableStateFlow(AppTimeWindow.HOUR_1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AppDischargeUiState> = flow {
        emit(batteryRepository.currentBatteryInfo().capacityMah ?: 4000)
    }.flatMapLatest { capacity ->
        combine(
            sessionManager.sessionHistory,
            settingsRepository.getSettings(),
            _selectedWindow,
            MutableStateFlow(permissionHelper.checkPermission())
        ) { history, settings, window, _ ->
            val now = System.currentTimeMillis()
            val cutoff = now - window.durationMs
            
            val filteredHistory = history.filter { it.endTime > cutoff }
            
            val aggregatedSessions = filteredHistory.groupBy { it.packageName }
                .filter { (packageName, _) ->
                    !settings.ignoreSystemProcesses || !PackageUtils.isSystemPackage(context, packageName)
                }
                .map { (packageName, sessions) ->
                    val totalMah = sessions.sumOf { it.totalMah }
                    val totalDurationMs = sessions.sumOf { 
                        val start = maxOf(it.startTime, cutoff)
                        val end = it.endTime
                        if (end > start) end - start else 0L
                    }
                    
                    val impactPercentage = if (filteredHistory.sumOf { it.totalMah } > 0) (totalMah / filteredHistory.sumOf { it.totalMah }) * 100.0 else 0.0
                    val capacityPercentage = (totalMah / capacity.toDouble()) * 100.0
                    val durationHours = totalDurationMs / 3600000.0
                    val drainRate = if (durationHours > 0) capacityPercentage / durationHours else 0.0

                    // Generate sparkline (20 buckets)
                    val buckets = 20
                    val bucketSize = window.durationMs / buckets
                    val sparkline = DoubleArray(buckets)
                    sessions.forEach { session ->
                        for (i in 0 until buckets) {
                            val bucketStart = cutoff + (i * bucketSize)
                            val bucketEnd = bucketStart + bucketSize
                            
                            val overlapStart = maxOf(session.startTime, bucketStart)
                            val overlapEnd = minOf(session.endTime, bucketEnd)
                            
                            if (overlapEnd > overlapStart) {
                                val sessionDuration = session.endTime - session.startTime
                                if (sessionDuration > 0) {
                                    val ratio = (overlapEnd - overlapStart).toDouble() / sessionDuration
                                    sparkline[i] += session.totalMah * ratio
                                }
                            }
                        }
                    }

                    val appName = getAppName(packageName)
                    val icon = try {
                        packageManager.getApplicationIcon(packageName)
                    } catch (_: Exception) {
                        null
                    }

                    AppDischargeUiModel(
                        packageName = packageName,
                        name = appName,
                        icon = icon,
                        totalMah = totalMah,
                        totalPercentage = impactPercentage,
                        drainRatePercentPerHour = drainRate,
                        durationMs = totalDurationMs,
                        sparklinePoints = sparkline.map { it.toFloat() }
                    )
                }
                .sortedByDescending { it.totalMah }
            
            val maxMah = aggregatedSessions.firstOrNull()?.totalMah ?: 1.0

            AppDischargeUiState(
                sessions = aggregatedSessions,
                maxMah = maxMah,
                totalTrackedMah = filteredHistory.sumOf { it.totalMah },
                selectedWindow = window,
                hasPermission = permissionHelper.checkPermission()
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppDischargeUiState()
    )

    fun setWindow(window: AppTimeWindow) {
        _selectedWindow.value = window
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(appInfo).toString()
            Timber.d("Resolved label for $packageName: $label")
            label
        } catch (e: Exception) {
            Timber.w("Failed to resolve label for $packageName: ${e.message}")
            packageName
        }
    }

    fun requestPermission(context: Context) {
        permissionHelper.requestPermission(context)
    }
}
