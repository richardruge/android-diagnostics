package com.creative.feature_battery.usage

import android.content.Context
import com.creative.core_model.ForegroundSession
import com.creative.feature_battery.data.history.AppUsageDao
import com.creative.feature_battery.data.history.toDomain
import com.creative.feature_battery.data.history.toEntity
import com.creative.core_system.usage.UsageSystemDataSource
import com.creative.feature_battery.domain.repository.BatteryRepository
import com.creative.feature_battery.domain.repository.BatterySettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class ForegroundSessionManager(
    private val usageDataSource: UsageSystemDataSource,
    private val batteryRepository: BatteryRepository,
    private val settingsRepository: BatterySettingsRepository,
    private val usageDao: AppUsageDao,
    private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    private val _currentSession = MutableStateFlow<SessionTracker?>(null)
    
    val foregroundAppFlow: Flow<String?> = usageDataSource.getForegroundAppFlow()

    private val _completedSessions = MutableSharedFlow<ForegroundSession>(extraBufferCapacity = 10)
    val completedSessions: SharedFlow<ForegroundSession> = _completedSessions.asSharedFlow()

    val sessionHistory: StateFlow<List<ForegroundSession>> = usageDao.observeAllUsage()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _currentPackage = MutableStateFlow<String?>(null)
    val currentPackage: StateFlow<String?> = _currentPackage.asStateFlow()

    init {
        startTracking()
    }

    private fun startTracking() {
        foregroundAppFlow
            .distinctUntilChanged()
            .onEach { packageName ->
                _currentPackage.value = packageName
                handleAppChange(packageName)
            }.launchIn(scope)

        batteryRepository.observeBatteryInfo().onEach { info ->
            _currentSession.value?.addSample(
                currentMa = info.currentNowMa?.toDouble() ?: 0.0
            )
        }.launchIn(scope)
    }

    private fun handleAppChange(packageName: String?) {
        scope.launch {
            mutex.withLock {
                val oldSession = _currentSession.value
                if (oldSession != null) {
                    val currentInfo = batteryRepository.currentBatteryInfo()
                    oldSession.addSample(currentInfo.currentNowMa?.toDouble() ?: 0.0)
                    
                    val session = oldSession.complete()
                    if (session != null) {
                        Timber.d("Completed session for ${session.packageName}: ${session.totalMah} mAh")
                        _completedSessions.emit(session)
                        usageDao.insert(session.toEntity())
                        cleanupOldSessions()
                    }
                }

                if (packageName != null) {
                    val newSession = SessionTracker(packageName)
                    val currentInfo = batteryRepository.currentBatteryInfo()
                    newSession.addSample(currentInfo.currentNowMa?.toDouble() ?: 0.0)
                    _currentSession.value = newSession
                } else {
                    _currentSession.value = null
                }
            }
        }
    }

    private suspend fun cleanupOldSessions() {
        try {
            val settings = settingsRepository.getSettings().first()
            val retentionMillis = settings.retentionMonths * 30L * 24 * 60 * 60 * 1000
            val cutoff = System.currentTimeMillis() - retentionMillis
            usageDao.deleteOlderThan(cutoff)
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup old sessions")
        }
    }

    private class SessionTracker(val packageName: String) {
        val startTime = System.currentTimeMillis()
        private val samples = mutableListOf<Double>()

        fun addSample(currentMa: Double) {
            samples.add(currentMa)
        }

        fun complete(): ForegroundSession? {
            if (samples.isEmpty()) return null
            val endTime = System.currentTimeMillis()
            val durationMs = endTime - startTime
            
            // Reduced threshold for easier testing
            if (durationMs < 1000) return null 

            val avgMa = samples.average()
            val durationHours = durationMs / (1000.0 * 3600.0)
            val totalMah = kotlin.math.abs(avgMa * durationHours)

            return ForegroundSession(
                packageName = packageName,
                startTime = startTime,
                endTime = endTime,
                avgMa = avgMa,
                totalMah = totalMah
            )
        }
    }
}
