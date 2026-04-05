package com.creative.core_system.network

import com.creative.core_model.NetworkState
import kotlinx.coroutines.flow.Flow

interface NetworkSystemDataSource {
    fun observeNetworkState(): Flow<NetworkState>
    fun getNetworkState(): NetworkState
    suspend fun runPingTest(host: String): Long?
}
