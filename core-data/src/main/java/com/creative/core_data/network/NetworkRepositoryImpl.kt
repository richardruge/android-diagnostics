package com.creative.core_data.network

import com.creative.core_model.NetworkState
import com.creative.core_system.network.NetworkSystemDataSource
import kotlinx.coroutines.flow.Flow

class NetworkRepositoryImpl(
    private val system: NetworkSystemDataSource
) : NetworkRepository {

    override fun observeNetworkState(): Flow<NetworkState> = system.observeNetworkState()

    override suspend fun getNetworkState(): NetworkState = system.getNetworkState()

    override suspend fun runPingTest(host: String): Long? = system.runPingTest(host)
}
