package com.creative.feature_battery.usage

import android.content.Context

object PackageUtils {
    fun isSystemPackage(context: Context, packageName: String?): Boolean {
        if (packageName == null) return false
        
        // Heuristic: If it has a launcher intent, it's likely a user-facing app, 
        // even if it's a system app or has a com.android prefix.
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) return false
        } catch (e: Exception) {
            // Fallback to prefix check if PM fails
        }

        return packageName == "android" ||
                packageName.startsWith("com.android.") ||
                packageName.startsWith("com.google.android.")
    }
}
