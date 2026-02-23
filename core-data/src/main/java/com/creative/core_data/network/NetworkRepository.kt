package com.creative.core_data.network

import com.creative.core_model.NetworkState

interface NetworkRepository {
    suspend fun getNetworkState(): NetworkState
}