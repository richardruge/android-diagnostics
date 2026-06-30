package com.creative.feature_battery.usage

import com.creative.core_model.ForegroundSession
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
    private val settingsRepository: BatterySettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    private val _currentSession = MutableStateFlow<SessionTracker?>(null)
    
    val foregroundAppFlow: Flow<String?> = usageDataSource.getForegroundAppFlow()

    private val _completedSessions = MutableSharedFlow<ForegroundSession>(extraBufferCapacity = 10)
    val completedSessions: SharedFlow<ForegroundSession> = _completedSessions.asSharedFlow()

    private val _sessionHistory = MutableStateFlow<List<ForegroundSession>>(emptyList())
    val sessionHistory: StateFlow<List<ForegroundSession>> = _sessionHistory.asStateFlow()

    private val _currentPackage = MutableStateFlow<String?>(null)
    val currentPackage: StateFlow<String?> = _currentPackage.asStateFlow()

    private val ignoreSystemFlow = settingsRepository.getSettings()
        .map { it.ignoreSystemProcesses }
        .distinctUntilChanged()

    init {
        startTracking()
    }

    private fun startTracking() {
        combine(
            foregroundAppFlow,
            ignoreSystemFlow
        ) { packageName, ignoreSystem ->
            if (ignoreSystem && isSystemPackage(packageName)) {
                null
            } else {
                packageName
            }
        }.distinctUntilChanged()
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

    private fun isSystemPackage(packageName: String?): Boolean {
        if (packageName == null) return false
        return packageName == "android" ||
                packageName.startsWith("com.android.") ||
                packageName.startsWith("com.google.android.")
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
                        _sessionHistory.update { (listOf(session) + it).take(50) }
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
