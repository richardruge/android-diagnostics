package com.creative.feature_network.domain.usecases

import com.creative.core_data.network.NetworkRepository

class GetNetworkStateUseCase(
    private val repo: NetworkRepository
) {
    suspend operator fun invoke() = repo.getNetworkState()
}