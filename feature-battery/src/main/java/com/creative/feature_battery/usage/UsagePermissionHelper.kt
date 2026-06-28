package com.creative.feature_battery.usage

import android.content.Context
import android.content.Intent
import com.creative.core_system.usage.UsageSystemDataSource

class UsagePermissionHelper(
    private val usageDataSource: UsageSystemDataSource
) {
    fun checkPermission(): Boolean {
        return usageDataSource.hasUsageStatsPermission()
    }

    fun requestPermission(context: Context) {
        val intent = usageDataSource.getUsageStatsPermissionIntent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
