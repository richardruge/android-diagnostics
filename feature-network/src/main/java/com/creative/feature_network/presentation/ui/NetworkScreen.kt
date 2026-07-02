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
                gatewayPingMs = uiState.gatewayPingMs,
                dnsPingMs = uiState.dnsPingMs,
                publicPingMs = uiState.publicPingMs,
                signalHistory = uiState.signalHistory,
                onRunPingTest = viewModel::runPingTest,
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
    gatewayPingMs: Long?,
    dnsPingMs: Long?,
    publicPingMs: Long?,
    signalHistory: List<Int>,
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
            MultiHopPingCard(isPingTesting, gatewayPingMs, dnsPingMs, publicPingMs, onRunPingTest)

            if (signalHistory.isNotEmpty()) {
                Text(
                    text = "Signal History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                SignalStrengthChart(history = signalHistory)
            }

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
private fun MultiHopPingCard(
    isTesting: Boolean,
    gatewayPing: Long?,
    dnsPing: Long?,
    publicPing: Long?,
    onRun: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network Diagnostics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onRun, enabled = !isTesting, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(if (isTesting) "Wait" else "Test All")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PingRow(label = "Local Gateway (Router)", ms = gatewayPing, isTesting = isTesting)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            PingRow(label = "DNS Server", ms = dnsPing, isTesting = isTesting)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            PingRow(label = "Public Internet (8.8.8.8)", ms = publicPing, isTesting = isTesting)
        }
    }
}

@Composable
private fun PingRow(label: String, ms: Long?, isTesting: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        val color = when {
            ms == null -> MaterialTheme.colorScheme.onSurface
            ms < 30 -> Color(0xFF4CAF50)
            ms < 100 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
        Text(
            text = if (isTesting && (ms == null)) "..." else if (ms != null) "$ms ms" else "-- ms",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
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

    val channel = state.frequencyMhz?.let { freq ->
        when (freq) {
            in 2412..2484 -> (freq - 2412) / 5 + 1
            in 5170..5825 -> (freq - 5170) / 5 + 34
            else -> null
        }
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailCard(label = "SSID", value = state.ssid ?: "Unknown")
        DetailCard(label = "Router MAC", value = state.bssid ?: "Unknown")
        DetailCard(
            label = "Signal Strength", 
            value = "${state.signalStrengthDbm} dBm ($signalRating)"
        )
        DetailCard(label = "Frequency", value = state.frequencyMhz?.let { "$it MHz" } ?: "Unknown")
        DetailCard(label = "Channel", value = channel?.toString() ?: "Unknown")
        state.wifiStandard?.let {
            DetailCard(label = "Protocol", value = it)
        }
        DetailCard(label = "IP Address", value = state.ipAddress ?: "Unknown")
        DetailCard(label = "Gateway", value = state.gatewayIp ?: "Unknown")
        DetailCard(label = "DNS Servers", value = state.dnsServers.joinToString("\n").ifEmpty { "Unknown" })
        DetailCard(label = "Link Speed", value = "${state.linkSpeedMbps} Mbps")
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}
