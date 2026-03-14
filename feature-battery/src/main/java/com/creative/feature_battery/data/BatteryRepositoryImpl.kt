package com.creative.feature_battery.data

import com.creative.core_system.battery.BatteryInfoProvider
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.repository.BatteryHistoryRepository
import com.creative.feature_battery.domain.repository.BatteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BatteryRepositoryImpl(
    private val provider: BatteryInfoProvider,
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

        provider.observe()
            .map { it.toBatteryInfo() }
            .onEach { info ->
                latest.value = info
                historyRepository.record(info)
            }
            .launchIn(scope)
    }

    override fun observeBatteryInfo(): Flow<BatteryInfo> = latest

    override suspend fun currentBatteryInfo(): BatteryInfo = latest.value
}
