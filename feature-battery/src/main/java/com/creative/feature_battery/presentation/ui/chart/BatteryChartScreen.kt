package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.creative.core_model.ThermalSeverity
import com.creative.core_model.ThermalStatus
import com.creative.feature_battery.domain.model.BatteryInfo
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.delay

@Composable
fun BatteryChartScreen(viewModel: BatteryChartViewModel) {
    val data by viewModel.chartData.collectAsState()
    val thermalStatus by viewModel.thermalStatus.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        thermalStatus?.let { status ->
            ThermalStatusHeader(status)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        BatteryChartContent(data = data, currentTemp = thermalStatus?.temperatureC)
    }
}

@Composable
private fun ThermalStatusHeader(status: ThermalStatus) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "System Thermal Status",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "${status.severity} (${status.temperatureC}°C)",
            style = MaterialTheme.typography.bodyLarge,
            color = when (status.severity) {
                ThermalSeverity.NORMAL -> MaterialTheme.colorScheme.primary
                ThermalSeverity.WARM -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BatteryChartContent(data: List<BatteryInfo>, currentTemp: Float? = null) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    // Window size (e.g., 5 minutes)
    val windowSizeSec = 5 * 60L
    
    var currentTimeSec by remember { mutableLongStateOf(System.currentTimeMillis() / 1000) }
    
    // Local cache for live points to bridge the gap between DB updates
    val livePoints = remember { mutableStateListOf<Pair<Long, Double>>() }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeSec = System.currentTimeMillis() / 1000
            delay(1000) 
        }
    }

    // Capture the current temperature into livePoints every second
    LaunchedEffect(currentTemp, currentTimeSec) {
        if (currentTemp != null) {
            val lastPoint = livePoints.lastOrNull()
            if (lastPoint == null || lastPoint.first < currentTimeSec) {
                livePoints.add(currentTimeSec to currentTemp.toDouble())
            } else if (lastPoint.first == currentTimeSec) {
                livePoints[livePoints.lastIndex] = currentTimeSec to currentTemp.toDouble()
            }
            
            // Cleanup old live points to avoid memory bloat
            val cutoff = currentTimeSec - windowSizeSec - 60 
            while (livePoints.isNotEmpty() && livePoints.first().first < cutoff) {
                livePoints.removeAt(0)
            }
        }
    }

    // Visible window boundaries in absolute seconds (Long)
    val maxX = currentTimeSec
    val minX = maxX - windowSizeSec
    
    // Merge historical data with live collected points
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

@Preview(showBackground = true)
@Composable
fun BatteryChartPreview() {
    BatteryChartContent(data = emptyList())
}
