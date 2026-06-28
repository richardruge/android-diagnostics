package com.creative.feature_battery.usage

import com.creative.core_model.ForegroundSession
import com.creative.core_system.usage.UsageSystemDataSource
import com.creative.feature_battery.domain.repository.BatteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ForegroundSessionManager(
    private val usageDataSource: UsageSystemDataSource,
    private val batteryRepository: BatteryRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentSession = MutableStateFlow<SessionTracker?>(null)
    
    // Expose the Flow<String?> as requested
    val foregroundAppFlow: Flow<String?> = usageDataSource.getForegroundAppFlow()

    // Completed sessions flow
    private val _completedSessions = MutableSharedFlow<ForegroundSession>(extraBufferCapacity = 10)
    val completedSessions: SharedFlow<ForegroundSession> = _completedSessions.asSharedFlow()

    init {
        startTracking()
    }

    private fun startTracking() {
        // Observe foreground app changes
        foregroundAppFlow.onEach { packageName ->
            handleAppChange(packageName)
        }.launchIn(scope)

        // Observe battery changes for the active session
        batteryRepository.observeBatteryInfo().onEach { info ->
            _currentSession.value?.addSample(
                currentMa = info.currentNowMa?.toDouble() ?: 0.0
            )
        }.launchIn(scope)
    }

    private fun handleAppChange(packageName: String?) {
        scope.launch {
            val oldSession = _currentSession.value
            if (oldSession != null) {
                val session = oldSession.complete()
                if (session != null) {
                    Timber.d("Completed session for ${session.packageName}: ${session.totalMah} mAh")
                    _completedSessions.emit(session)
                }
            }

            if (packageName != null) {
                _currentSession.value = SessionTracker(packageName)
            } else {
                _currentSession.value = null
            }
        }
    }

    private class SessionTracker(val packageName: String) {
        val startTime = System.currentTimeMillis()
        private val samples = mutableListOf<Double>()

        fun addSample(currentMa: Double) {
            // Negative value means discharge.
            samples.add(currentMa)
        }

        fun complete(): ForegroundSession? {
            if (samples.isEmpty()) return null
            val endTime = System.currentTimeMillis()
            val durationMs = endTime - startTime
            if (durationMs < 2000) return null // Ignore very short sessions

            val avgMa = samples.average()
            val durationHours = durationMs / (1000.0 * 3600.0)
            
            // We calculate total device drain during this session.
            // If avgMa is -300mA and duration is 1 hour, drain is 300mAh.
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
