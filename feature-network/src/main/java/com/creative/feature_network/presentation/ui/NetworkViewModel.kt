package com.creative.feature_network.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creative.core_data.network.NetworkRepository
import com.creative.core_model.NetworkType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NetworkViewModel(
    private val repository: NetworkRepository,
) : ViewModel() {

    private val _pingHistory = MutableStateFlow<List<Long>>(emptyList())
    private val _signalHistory = MutableStateFlow<List<Int>>(emptyList())
    private val _isPingTesting = MutableStateFlow(value = false)
    private val _multiHopResults = MutableStateFlow<Triple<Long?, Long?, Long?>>(Triple(null, null, null))

    val uiState: StateFlow<NetworkUiState> = combine(
        repository.observeNetworkState().onEach { state ->
            state.signalStrengthDbm?.let { dbm ->
                _signalHistory.update { (it + dbm).takeLast(50) }
            }
        },
        _pingHistory,
        _signalHistory,
        _isPingTesting,
        _multiHopResults
    ) { state, pHistory, sHistory, testing, hops ->
        NetworkUiState(
            networkState = state,
            isPingTesting = testing,
            pingHistory = pHistory,
            signalHistory = sHistory,
            gatewayPingMs = hops.first,
            dnsPingMs = hops.second,
            publicPingMs = hops.third,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkUiState(isLoading = true)
    )

    fun runPingTest() {
        viewModelScope.launch {
            val currentState = repository.getNetworkState()
            _isPingTesting.value = true
            
            // 1. Gateway
            val gatewayPing = currentState.gatewayIp?.let { repository.runPingTest(it) }
            
            // 2. DNS
            val dnsPing = currentState.dnsServers.firstOrNull()?.let { repository.runPingTest(it) }
            
            // 3. Public (Google)
            val publicPing = repository.runPingTest("8.8.8.8")

            _multiHopResults.value = Triple(gatewayPing, dnsPing, publicPing)
            
            publicPing?.let { result ->
                _pingHistory.update { (it + result).takeLast(20) }
            }

            _isPingTesting.value = false
        }
    }
}
