package com.creative.feature_battery.presentation

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.feature_battery.usage.ForegroundSessionManager
import com.creative.feature_battery.usage.UsagePermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

data class AppDischargeUiState(
    val sessions: List<AppDischargeUiModel> = emptyList(),
    val currentApp: String? = null,
    val hasPermission: Boolean = true
)

data class AppDischargeUiModel(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val totalMah: Double,
    val avgMa: Double,
    val durationMs: Long
)

class AppDischargeViewModel(
    private val sessionManager: ForegroundSessionManager,
    private val permissionHelper: UsagePermissionHelper,
    private val context: Context
) : ViewModel() {

    private val packageManager = context.packageManager

    val uiState: StateFlow<AppDischargeUiState> = combine(
        sessionManager.sessionHistory,
        sessionManager.currentPackage,
        MutableStateFlow(permissionHelper.checkPermission())
    ) { history, current, _ ->
        val aggregatedSessions = history.groupBy { it.packageName }
            .map { (packageName, sessions) ->
                val totalMah = sessions.sumOf { it.totalMah }
                val totalDurationMs = sessions.sumOf { it.endTime - it.startTime }
                val avgMa = if (totalDurationMs > 0) {
                    totalMah / (totalDurationMs / 3600000.0)
                } else {
                    0.0
                }

                val appName = getAppName(packageName)
                val icon = try {
                    packageManager.getApplicationIcon(packageName)
                } catch (e: Exception) {
                    null
                }

                AppDischargeUiModel(
                    packageName = packageName,
                    name = appName,
                    icon = icon,
                    totalMah = totalMah,
                    avgMa = avgMa,
                    durationMs = totalDurationMs
                )
            }
            .sortedByDescending { it.totalMah }

        AppDischargeUiState(
            sessions = aggregatedSessions,
            currentApp = current?.let { getAppName(it) },
            hasPermission = permissionHelper.checkPermission()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppDischargeUiState()
    )

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
