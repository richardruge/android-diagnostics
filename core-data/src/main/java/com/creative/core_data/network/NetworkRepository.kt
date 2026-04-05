package com.creative.core_data.network

import com.creative.core_model.NetworkState
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    fun observeNetworkState(): Flow<NetworkState>
    suspend fun getNetworkState(): NetworkState
    suspend fun runPingTest(host: String = "8.8.8.8"): Long?
}
