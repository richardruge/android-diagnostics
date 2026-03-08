package com.creative.feature_battery.data

import com.creative.core_system.battery.BatteryHealth
import com.creative.core_system.battery.BatteryInfoProvider
import com.creative.core_system.battery.RawBatteryInfo
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.repository.BatteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class BatteryRepositoryImpl(
    private val provider: BatteryInfoProvider
) : BatteryRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val latest: MutableStateFlow<BatteryInfo> =
        MutableStateFlow(provider.readCurrent().toBatteryInfo())

    init {
        provider.observe()
            .map { it.toBatteryInfo() }
            .onEach { latest.value = it }
            .launchIn(scope)
    }

    override fun observeBatteryInfo(): Flow<BatteryInfo> = latest

    override suspend fun currentBatteryInfo(): BatteryInfo = latest.value
}