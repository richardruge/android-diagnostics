package com.creative.core_system.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

interface UsageSystemDataSource {
    fun getForegroundAppFlow(pollIntervalMs: Long = 1000): Flow<String?>
    fun hasUsageStatsPermission(): Boolean
    fun getUsageStatsPermissionIntent(): Intent
}

class UsageSystemDataSourceImpl(
    private val context: Context
) : UsageSystemDataSource {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    override fun getForegroundAppFlow(pollIntervalMs: Long): Flow<String?> = flow {
        while (true) {
            emit(getForegroundPackageName())
            delay(500)
        }
    }.distinctUntilChanged()

    private fun getForegroundPackageName(): String? {
        val time = System.currentTimeMillis()
        
        // 1. Try UsageEvents for real-time accuracy
        val usageEvents = try {
            usageStatsManager.queryEvents(time - 1000 * 60, time)
        } catch (e: Exception) {
            null
        }
        
        if (usageEvents != null) {
            val event = UsageEvents.Event()
            var lastForegroundApp: String? = null
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastForegroundApp = event.packageName
                }
            }
            if (lastForegroundApp != null) return lastForegroundApp
        }

        // 2. Fallback to queryUsageStats
        val stats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 60,
                time
            )
        } catch (e: Exception) {
            null
        }
        
        return stats?.filter { it.lastTimeUsed > 0 }
            ?.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    override fun hasUsageStatsPermission(): Boolean {
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun getUsageStatsPermissionIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }
}
