package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creative.core_model.ThermalStatus
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.model.ChargingRate
import com.creative.feature_battery.domain.model.Severity
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryChartScreen(
    viewModel: BatteryChartViewModel = koinViewModel()
) {
    val latestInfo by viewModel.batteryStatus.collectAsStateWithLifecycle()
    val healthSeverity by viewModel.batteryHealthSeverity.collectAsStateWithLifecycle()
    val thermalStatus by viewModel.thermalStatus.collectAsStateWithLifecycle()
    val selectedWindow by viewModel.selectedWindow.collectAsStateWithLifecycle()
    
    // Use unified chartUiState to ensure data and its timeframe are always in sync
    val chartUiState by viewModel.chartUiState.collectAsStateWithLifecycle()

    // Sync producers with data state
    // Keying on data AND window ensures the graph refreshes its viewport even if data list is same
    LaunchedEffect(chartUiState.data, chartUiState.window) {
        viewModel.batteryLevelModelProducer.runTransaction {
            lineSeries {
                series(
                    chartUiState.data.map { it.timestamp.toDouble() },
                    chartUiState.data.map { it.level.toDouble() }
                )
            }
        }
        viewModel.temperatureModelProducer.runTransaction {
            lineSeries {
                series(
                    chartUiState.data.map { it.timestamp.toDouble() },
                    chartUiState.data.map { it.temperatureC.toDouble() }
                )
            }
        }
    }

    // Calculate chart bounds based on the state actually used for filtering.
    // Keying on chartUiState ensures that if either data or window changes, bounds are recalculated.
    // Use raw timestamps (milliseconds) for bounds.
    val maxX = chartUiState.endTimestamp.toDouble()
    val minX = maxX - (chartUiState.window.minutes * 60 * 1000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Battery Trends",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        TimeWindowSelector(
            selectedWindow = selectedWindow,
            onWindowSelected = viewModel::setWindow
        )

        // Real-time Power & Health Summary
        RealTimeMetricsSection(latestInfo, healthSeverity, thermalStatus)

        // Charger Rating (if charging)
        ChargerRatingCard(latestInfo)

        if (chartUiState.data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No data available for the selected period", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            // Battery Level Chart
            key(chartUiState.window) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Battery Level (%)", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        BatteryLevelChart(
                            modelProducer = viewModel.batteryLevelModelProducer,
                            runAnimations = false,
                            minX = minX,
                            maxX = maxX
                        )
                    }
                }
            }

            // Temperature Chart
            key(chartUiState.window) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Temperature (°C)", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        TemperatureChart(
                            modelProducer = viewModel.temperatureModelProducer,
                            runAnimations = false,
                            minX = minX,
                            maxX = maxX
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeWindowSelector(
    selectedWindow: TimeWindow,
    onWindowSelected: (TimeWindow) -> Unit
) {
    val options = TimeWindow.entries
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, window ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onWindowSelected(window) },
                selected = window == selectedWindow,
                label = {
                    Text(
                        text = when (window) {
                            TimeWindow.MIN_15 -> "15m"
                            TimeWindow.HOUR_1 -> "1h"
                            TimeWindow.HOUR_24 -> "24h"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun ChargerRatingCard(info: BatteryInfo?) {
    if (info == null) return

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (info.isCharging) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Charger Rating",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (info.isCharging) {
                    val infiniteTransition = rememberInfiniteTransition(label = "bolt")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50).copy(alpha = alpha),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (info.isCharging) {
                val currentPower = info.powerW ?: 0.0
                val maxPower = info.maxPowerW ?: 0.0
                val progress = if (maxPower > 0) (currentPower / maxPower).toFloat().coerceIn(0f, 1f) else 0f
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (progress > 0.8f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Current: ${"%.1f".format(currentPower)}W",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Negotiated: ${"%.0f".format(maxPower)}W",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = "No Charger Connected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RealTimeMetricsSection(
    info: BatteryInfo?, 
    healthSeverity: Severity,
    thermalStatus: ThermalStatus?
) {
    if (info == null) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Real-time Metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Row 1: Power & Health
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = if (info.isCharging) "Charging Speed" else "Power Draw",
                value = if (info.isCharging && info.chargingRate != ChargingRate.NONE) {
                    info.chargingRate.name.replace("_", " ")
                } else {
                    info.powerW?.let { "%.2f W".format(it) } ?: "N/A"
                },
                subtitle = if (info.isCharging) "Charging" else "On Battery",
                color = if (info.isCharging) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondary
            )

            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Battery Health",
                value = info.health.name,
                subtitle = "Severity: ${healthSeverity.name}",
                color = when (healthSeverity) {
                    Severity.CRITICAL, Severity.HIGH -> MaterialTheme.colorScheme.error
                    Severity.MEDIUM -> Color(0xFFFFA000)
                    else -> Color(0xFF4CAF50)
                }
            )
        }

        // Row 2: Voltage & Capacity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Voltage",
                value = info.voltageMv?.let { "${"%.2f".format(it / 1000f)} V" } ?: "N/A",
                subtitle = info.technology ?: "Lithium-ion",
                color = MaterialTheme.colorScheme.tertiary
            )

            val wearSubtitle = when {
                info.stateOfHealth != null -> "Wear: ${100 - info.stateOfHealth}%"
                info.cycleCount != null -> "Cycles: ${info.cycleCount}"
                else -> info.technology ?: "Design Cap."
            }

            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Capacity",
                value = info.capacityMah?.let { "$it mAh" } ?: "N/A",
                subtitle = wearSubtitle,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Row 3: Thermal Status
        thermalStatus?.let { thermal ->
            MetricCard(
                modifier = Modifier.fillMaxWidth(),
                title = "System Thermal Status",
                value = thermal.severity.name,
                subtitle = "Source: ${thermal.status} (${"%.1f".format(thermal.temperatureC)}°C)",
                color = when (thermal.severity.name) {
                    "CRITICAL" -> MaterialTheme.colorScheme.error
                    "HOT" -> Color(0xFFFFA000)
                    else -> Color(0xFF4CAF50)
                }
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(color.copy(alpha = 0.3f))
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
