package com.creative.feature_battery.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creative.feature_battery.domain.model.ChargingRate
import com.creative.feature_battery.presentation.BatteryUiState
import com.creative.feature_battery.presentation.BatteryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (uiState) {
        is BatteryUiState.Loading -> LoadingView()
        is BatteryUiState.Ready -> {
            val readyState = uiState as BatteryUiState.Ready
            Column {
                if (!readyState.hasUsagePermission) {
                    PermissionWarning(onGrant = { viewModel.requestUsagePermission(context) })
                }
                BatteryContent(readyState)
            }
        }
    }
}

@Composable
private fun PermissionWarning(onGrant: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Usage Access permission is required for app tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onGrant) {
                Text("Grant")
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BatteryContent(state: BatteryUiState.Ready) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Battery Status",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        InfoCard(label = "Level", value = "${state.level}%")
        InfoCard(label = "Health", value = state.health)
        InfoCard(label = "Temperature", value = "${state.temperatureC}°C")
        InfoCard(label = "Charging", value = if (state.isCharging) "Yes" else "No")
        InfoCard(label = "Foreground App", value = state.foregroundPackageName ?: "Unknown/System")
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text(
            text = "Power Consumption",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        val currentSign = if (state.isCharging) "+" else "-"
        InfoCard(
            label = "Current Now", 
            value = state.currentNowMa?.let { "$currentSign${Math.abs(it)} mA" } ?: "N/A"
        )
        InfoCard(
            label = "Average Current", 
            value = state.currentAverageMa?.let { "$currentSign${Math.abs(it)} mA" } ?: "N/A"
        )
        
        state.voltageMv?.let { voltage ->
            state.currentNowMa?.let { current ->
                val powerW = (voltage.toFloat() / 1000f) * (Math.abs(current).toFloat() / 1000f)
                InfoCard(label = "Power", value = "%.2f W".format(powerW))
            }
        }

        if (state.isCharging && state.chargingRate != ChargingRate.NONE) {
            InfoCard(
                label = "Charging Speed",
                value = state.chargingRate.name.replace("_", " ")
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text(
            text = "Advanced Metrics",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        InfoCard(label = "Cycle Count", value = state.cycleCount?.toString() ?: "N/A")
        InfoCard(label = "State of Health", value = state.stateOfHealth?.let { "$it%" } ?: "N/A")
        InfoCard(label = "Capacity", value = state.capacityMah?.let { "$it mAh" } ?: "N/A")
        InfoCard(label = "Voltage", value = state.voltageMv?.let { "$it mV" } ?: "N/A")
        InfoCard(label = "Technology", value = state.technology ?: "N/A")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "System Severity: ${state.severity}",
            style = MaterialTheme.typography.bodyLarge,
            color = when (state.severity.name) {
                "CRITICAL", "HIGH" -> MaterialTheme.colorScheme.error
                "MEDIUM", "LOW" -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
