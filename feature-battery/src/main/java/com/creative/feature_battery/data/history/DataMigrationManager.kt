package com.creative.feature_battery.data.history

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import timber.log.Timber
import androidx.core.content.edit

class DataMigrationManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("data_migration_prefs", Context.MODE_PRIVATE)

    /**
     * Checks if the database on disk has a lower version than the current code version.
     */
    fun checkMigrationNeeded(dbName: String, currentVersion: Int): Boolean {
        val dbPath = context.getDatabasePath(dbName)
        if (!dbPath.exists()) return false

        return try {
            val db = SQLiteDatabase.openDatabase(dbPath.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val oldVersion = db.version
            db.close()
            
            if (oldVersion > 0 && oldVersion < currentVersion) {
                Timber.i("Migration needed for $dbName: $oldVersion -> $currentVersion")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking DB version for $dbName")
            false
        }
    }

    fun setMigrationChoicePending(dbName: String, pending: Boolean) {
        prefs.edit { putBoolean("pending_$dbName", pending) }
    }

    fun isMigrationChoicePending(dbName: String): Boolean {
        return prefs.getBoolean("pending_$dbName", false)
    }

    fun requestWipe(dbName: String) {
        prefs.edit { putBoolean("wipe_$dbName", true) }
    }

    fun isWipeRequested(dbName: String): Boolean {
        return prefs.getBoolean("wipe_$dbName", false)
    }

    fun clearWipeRequest(dbName: String) {
        prefs.edit { remove("wipe_$dbName") }
    }
    
    fun markMigrationHandled(dbName: String) {
        prefs.edit { putBoolean("pending_$dbName", false) }
    }
}
