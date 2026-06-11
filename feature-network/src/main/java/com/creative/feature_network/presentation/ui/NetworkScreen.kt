package com.creative.feature_network.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.creative.core_model.NetworkState
import com.creative.core_model.NetworkType
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkScreen(viewModel: NetworkViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> LoadingView()
            uiState.errorMessage != null -> ErrorView(uiState.errorMessage!!)
            uiState.networkState != null -> NetworkContent(
                state = uiState.networkState!!,
                isPingTesting = uiState.isPingTesting,
                lastPingMs = uiState.lastPingMs,
                pingHistory = uiState.pingHistory,
                onRunPingTest = { viewModel.runPingTest() }
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun NetworkContent(
    state: NetworkState,
    isPingTesting: Boolean,
    lastPingMs: Long?,
    pingHistory: List<Long>,
    onRunPingTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ConnectionStatusCard(state)

        if (state.isConnected) {
            LatencyTestCard(isPingTesting, lastPingMs, pingHistory, onRunPingTest)

            Text(
                text = "Connection Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            if (state.type == NetworkType.WIFI) {
                WifiDetails(state)
            } else if (state.type == NetworkType.CELLULAR) {
                CellularDetails(state)
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(state: NetworkState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (state.isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (state.type) {
                    NetworkType.WIFI -> Icons.Default.Wifi
                    NetworkType.CELLULAR -> Icons.Default.CellTower
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (state.isConnected) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Type: ${state.type.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LatencyTestCard(
    isTesting: Boolean,
    lastPingMs: Long?,
    pingHistory: List<Long>,
    onRun: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Latency (Speed Test)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val pingText = when {
                        isTesting -> "Testing..."
                        lastPingMs != null -> "$lastPingMs ms"
                        else -> "-- ms"
                    }
                    val pingColor = when {
                        lastPingMs == null -> MaterialTheme.colorScheme.onSurface
                        lastPingMs < 50 -> Color(0xFF4CAF50)
                        lastPingMs < 150 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                    
                    Text(
                        text = pingText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isTesting) MaterialTheme.colorScheme.onSurfaceVariant else pingColor
                    )
                    
                    Text(
                        text = "8.8.8.8 (Google DNS)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (pingHistory.isNotEmpty()) {
                    LatencySparkline(
                        history = pingHistory,
                        modifier = Modifier
                            .height(40.dp)
                            .width(80.dp)
                            .padding(horizontal = 8.dp),
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                }
                
                Button(
                    onClick = onRun,
                    enabled = !isTesting
                ) {
                    Text(if (isTesting) "Wait" else "Run Test")
                }
            }
        }
    }
}

@Composable
private fun WifiDetails(state: NetworkState) {
    val signalRating = when (state.signalLevel) {
        4 -> "Excellent"
        3 -> "Good"
        2 -> "Fair"
        1 -> "Poor"
        else -> "Very Poor"
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailCard(label = "SSID: ", value = state.ssid ?: "Unknown")
        DetailCard(
            label = "Signal Strength: ", 
            value = "${state.signalStrengthDbm} dBm ($signalRating)"
        )
        val frequencyValue = if (state.frequencyMhz != null) {
            "${state.frequencyMhz} MHz" + (state.wifiStandard?.let { " ($it)" } ?: "")
        } else {
            "Unknown"
        }
        DetailCard(label = "Frequency: ", value = frequencyValue)
        DetailCard(label = "Connection Rate: ", value = "${state.linkSpeedMbps} Mbps")
    }
}

@Composable
private fun CellularDetails(state: NetworkState) {
    val signalRating = when (state.signalLevel) {
        4 -> "Excellent"
        3 -> "Good"
        2 -> "Fair"
        1 -> "Poor"
        else -> "Very Poor"
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailCard(label = "Signal Level", value = "${state.signalLevel}/4 ($signalRating)")
        state.signalStrengthDbm?.let {
            DetailCard(label = "Signal Strength", value = "$it dBm ($signalRating)")
        }
    }
}

@Composable
private fun DetailCard(label: String, value: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
