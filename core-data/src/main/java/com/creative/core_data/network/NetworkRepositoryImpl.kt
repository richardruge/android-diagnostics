package com.creative.core_data.network

import com.creative.core_model.NetworkHealth
import com.creative.core_model.NetworkState
import com.creative.core_system.network.NetworkSystemDataSource

class NetworkRepositoryImpl(
    private val system: NetworkSystemDataSource
) : NetworkRepository {

    override suspend fun getNetworkState(): NetworkState {
        // Phase 0 placeholder
        return NetworkState(
            levelPercent = 0,
            health = NetworkHealth.UNKNOWN,
            isCharging = false
        )
    }
}