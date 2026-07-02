package com.creative.feature_battery.data

import com.creative.core_system.battery.BatteryInfoProvider
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
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

        val databaseBatch = mutableListOf<BatteryInfo>()
        provider.observe()
            .map { it.toBatteryInfo() }
            .onEach { info ->
                latest.value = info
            }
            // Sample every 15 seconds (4x per minute)
            .sample(15.seconds)
            .onEach { info ->
                databaseBatch.add(info)
                // Write to database once we have 4 samples (1x per minute)
                if (databaseBatch.size >= 4) {
                    val toRecord = databaseBatch.toList()
                    databaseBatch.clear()
                    historyRepository.record(toRecord)
                }
            }
            .launchIn(scope)
    }

    override fun observeBatteryInfo(): Flow<BatteryInfo> = latest

    override suspend fun currentBatteryInfo(): BatteryInfo = latest.value
}
