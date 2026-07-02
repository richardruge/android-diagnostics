package com.creative.feature_battery.data

import com.creative.core_system.battery.BatteryInfoProvider
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
class BatteryRepositoryImpl(
    provider: BatteryInfoProvider,
    private val historyRepository: BatteryHistoryRepository
) : BatteryRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val initialInfo = provider.readCurrent().toBatteryInfo()
    private val latest: MutableStateFlow<BatteryInfo> = MutableStateFlow(initialInfo)

    init {
        // Record the very first value immediately so the chart isn't empty
        scope.launch {
            historyRepository.record(initialInfo)
        }

        // 1. Keep 'latest' updated as fast as the system provides info
        provider.observe()
            .onEach { latest.value = it.toBatteryInfo() }
            .launchIn(scope)

        // 2. Separately, record to database at a fixed interval
        // This ensures data is saved even if the system doesn't send broadcasts (e.g. idle battery)
        scope.launch {
            val databaseBatch = mutableListOf<BatteryInfo>()
            while (true) {
                delay(15.seconds)
                databaseBatch.add(latest.value)
                
                if (databaseBatch.size >= 4) {
                    val toRecord = databaseBatch.toList()
                    databaseBatch.clear()
                    historyRepository.record(toRecord)
                    Timber.d("Recorded ${toRecord.size} battery samples to database")
                }
            }
        }
    }

    override fun observeBatteryInfo(): Flow<BatteryInfo> = latest

    override suspend fun currentBatteryInfo(): BatteryInfo = latest.value
}
