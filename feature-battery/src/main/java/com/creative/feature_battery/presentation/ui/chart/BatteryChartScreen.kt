package com.creative.feature_battery.presentation.ui.chart

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.creative.core_model.ThermalSeverity
import com.creative.core_model.ThermalStatus
import com.creative.feature_battery.domain.model.BatteryInfo
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun BatteryChartScreen(viewModel: BatteryChartViewModel) {
    val data by viewModel.chartData.collectAsState()
    val thermalStatus by viewModel.thermalStatus.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        thermalStatus?.let { status ->
            ThermalStatusHeader(status)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        BatteryChartContent(data = data)
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
fun BatteryChartContent(data: List<BatteryInfo>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    // Mock data only as a fallback for the preview or if history is completely empty
    val mockValues = listOf(32f, 33.5f, 31f, 34f, 35.2f, 36f, 34.5f, 33f, 35f, 37f)

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(data.toValues())
                }
            }
        } else {
            // Optional: Handle empty state (e.g., still show mock or a message)
            modelProducer.runTransaction {
                lineSeries { series(mockValues) }
            }
        }
    }

    TemperatureChart(
        modelProducer = modelProducer
    )
}

@Preview(showBackground = true)
@Composable
fun BatteryChartPreview() {
    BatteryChartContent(data = emptyList())
}
