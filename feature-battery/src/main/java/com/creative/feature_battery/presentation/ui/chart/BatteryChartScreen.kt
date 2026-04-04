package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.creative.core_model.ThermalSeverity
import com.creative.core_model.ThermalStatus
import com.creative.feature_battery.domain.model.BatteryInfo
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BatteryChartScreen(viewModel: BatteryChartViewModel) {
    val data by viewModel.chartData.collectAsState()
    val thermalStatus by viewModel.thermalStatus.collectAsState()
    val selectedWindow by viewModel.selectedWindow.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        thermalStatus?.let { status ->
            ThermalStatusHeader(status)
            Spacer(modifier = Modifier.height(16.dp))
        }

        alerts.forEach { alert ->
            AlertItem(alert)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TimeWindowSelector(
            selectedWindow = selectedWindow,
            onWindowSelected = { viewModel.setWindow(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Temperature History",
                style = MaterialTheme.typography.titleMedium
            )
            LiveIndicator()
        }

        Spacer(modifier = Modifier.height(16.dp))

        BatteryChartContent(
            data = data,
            currentTemp = thermalStatus?.temperatureC,
            windowMinutes = selectedWindow.minutes
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Battery Level History",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        BatteryLevelChartContent(
            data = data,
            windowMinutes = selectedWindow.minutes
        )

        Spacer(modifier = Modifier.height(8.dp))
        
        LastUpdatedTimestamp(thermalStatus?.timestamp)
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AlertItem(alert: DiagnosticAlert) {
    val containerColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
        AlertSeverity.WARNING -> Color(0xFFFFECB3) // Amber/Yellow
        AlertSeverity.INFO -> MaterialTheme.colorScheme.primaryContainer
    }
    
    val contentColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
        AlertSeverity.WARNING -> Color(0xFF5D4037) // Dark Brown
        AlertSeverity.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val icon = when (alert.severity) {
        AlertSeverity.CRITICAL -> Icons.Default.Warning
        AlertSeverity.WARNING -> Icons.Default.Warning
        AlertSeverity.INFO -> Icons.Default.Info
    }

    AnimatedVisibility(
        visible = true,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(containerColor)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_indicator")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(alpha)
                .clip(CircleShape)
                .background(Color.Red)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
    }
}

@Composable
private fun TimeWindowSelector(
    selectedWindow: TimeWindow,
    onWindowSelected: (TimeWindow) -> Unit
) {
    val options = TimeWindow.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, window ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onWindowSelected(window) },
                selected = window == selectedWindow
            ) {
                Text(
                    when (window) {
                        TimeWindow.MIN_15 -> "15m"
                        TimeWindow.HOUR_1 -> "1h"
                        TimeWindow.HOUR_24 -> "24h"
                    }
                )
            }
        }
    }
}

@Composable
private fun LastUpdatedTimestamp(timestamp: Long?) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = timestamp?.let { formatter.format(Date(it)) } ?: "--:--:--"
    
    Text(
        text = "Last updated: $timeString",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.End
    )
}

@Composable
private fun ThermalStatusHeader(status: ThermalStatus) {
    val statusColor = when (status.severity) {
        ThermalSeverity.NORMAL -> MaterialTheme.colorScheme.primary
        ThermalSeverity.WARM -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (status.severity == ThermalSeverity.NORMAL)
                    Icons.Default.Thermostat else Icons.Default.Warning,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column {
                Text(
                    text = "System Thermal Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = status.severity.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${status.temperatureC}°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BatteryChartContent(
    data: List<BatteryInfo>,
    currentTemp: Float? = null,
    windowMinutes: Long = 5
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    val windowSizeSec = windowMinutes * 60L
    
    var currentTimeSec by remember { mutableLongStateOf(System.currentTimeMillis() / 1000) }
    
    val livePoints = remember { mutableStateListOf<Pair<Long, Double>>() }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeSec = System.currentTimeMillis() / 1000
            delay(1000) 
        }
    }

    LaunchedEffect(currentTemp, currentTimeSec) {
        if (currentTemp != null) {
            val lastPoint = livePoints.lastOrNull()
            if (lastPoint == null || lastPoint.first < currentTimeSec) {
                livePoints.add(currentTimeSec to currentTemp.toDouble())
            } else if (lastPoint.first == currentTimeSec) {
                livePoints[livePoints.lastIndex] = currentTimeSec to currentTemp.toDouble()
            }
            
            val cutoff = currentTimeSec - windowSizeSec - 60 
            while (livePoints.isNotEmpty() && livePoints.first().first < cutoff) {
                livePoints.removeAt(0)
            }
        }
    }

    val maxX = currentTimeSec
    val minX = maxX - windowSizeSec
    
    val chartPoints = remember(data, livePoints.size, minX) { 
        val historical = data.filter { it.timestamp >= (minX * 1000) }
            .map { (it.timestamp / 1000) to it.temperatureC.toDouble() }
            
        (historical + livePoints)
            .filter { it.first >= minX }
            .distinctBy { it.first }
            .sortedBy { it.first }
    }

    val minY = remember(chartPoints) {
        if (chartPoints.isNotEmpty()) {
            val minInWindow = chartPoints.minOf { it.second }
            val latestTemp = chartPoints.last().second
            minOf(minInWindow, latestTemp - 10.0)
        } else {
            null
        }
    }
    
    LaunchedEffect(chartPoints, minX) {
        if (chartPoints.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = chartPoints.map { it.first },
                        y = chartPoints.map { it.second }
                    )
                }
            }
        }
    }

    if (chartPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No data available", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        TemperatureChart(
            modelProducer = modelProducer,
            minX = minX.toDouble(),
            maxX = maxX.toDouble(),
            minY = minY,
            runAnimations = false
        )
    }
}

@Composable
fun BatteryLevelChartContent(
    data: List<BatteryInfo>,
    windowMinutes: Long = 5
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val windowSizeSec = windowMinutes * 60L
    var currentTimeSec by remember { mutableLongStateOf(System.currentTimeMillis() / 1000) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeSec = System.currentTimeMillis() / 1000
            delay(1000)
        }
    }

    val maxX = currentTimeSec
    val minX = maxX - windowSizeSec

    val chartPoints = remember(data, minX) {
        data.filter { it.timestamp >= (minX * 1000) }
            .map { (it.timestamp / 1000) to (it.level.toDouble()) }
            .distinctBy { it.first }
            .sortedBy { it.first }
    }

    val minY = remember(chartPoints) {
        if (chartPoints.isNotEmpty()) {
            val min = chartPoints.minOf { it.second }
            maxOf(0.0, min - 2.0)
        } else 0.0
    }

    val maxY = remember(chartPoints) {
        if (chartPoints.isNotEmpty()) {
            val max = chartPoints.maxOf { it.second }
            minOf(100.0, max + 2.0)
        } else 100.0
    }

    LaunchedEffect(chartPoints, minX) {
        if (chartPoints.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = chartPoints.map { it.first },
                        y = chartPoints.map { it.second }
                    )
                }
            }
        }
    }

    if (chartPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No level data available", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        BatteryLevelChart(
            modelProducer = modelProducer,
            minX = minX.toDouble(),
            maxX = maxX.toDouble(),
            minY = minY,
            maxY = maxY,
            runAnimations = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryChartPreview() {
    BatteryChartContent(data = emptyList())
}
