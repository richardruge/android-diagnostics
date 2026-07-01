package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryLongTermScreen(
    viewModel: BatteryLongTermViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedWindow by viewModel.selectedWindow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Long-term Battery Trends",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        LongTermWindowSelector(
            selectedWindow = selectedWindow,
            onWindowSelected = viewModel::setWindow
        )

        val minX = uiState.startTimestamp.toDouble()
        val maxX = uiState.endTimestamp.toDouble()

        if (uiState.aggregations.size < 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Insufficient aggregated data for this period",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            key(selectedWindow) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Average Battery Level (%)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LongTermBatteryChart(
                            modelProducer = viewModel.avgLevelModelProducer,
                            lineColor = Color(0xFF4CAF50),
                            valueSuffix = "%",
                            minX = minX,
                            maxX = maxX
                        )
                    }
                }
            }

            key(selectedWindow) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Average Temperature (°C)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LongTermBatteryChart(
                            modelProducer = viewModel.avgTempModelProducer,
                            lineColor = Color(0xFFF44336),
                            valueSuffix = "°C",
                            minX = minX,
                            maxX = maxX
                        )
                    }
                }
            }

            key(selectedWindow) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Average Voltage (V)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LongTermBatteryChart(
                            modelProducer = viewModel.avgVoltageModelProducer,
                            lineColor = Color(0xFF2196F3),
                            valueSuffix = "V",
                            isVoltage = true,
                            minX = minX,
                            maxX = maxX
                        )
                    }
                }
            }

            key(selectedWindow) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Average Current (mA)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LongTermBatteryChart(
                            modelProducer = viewModel.avgCurrentModelProducer,
                            lineColor = Color(0xFFFF9800),
                            valueSuffix = "mA",
                            minX = minX,
                            maxX = maxX
                        )
                    }
                }
            }
            
            Text(
                text = "Aggregated data is collected every 30 minutes and kept based on your settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongTermWindowSelector(
    selectedWindow: LongTermWindow,
    onWindowSelected: (LongTermWindow) -> Unit
) {
    val options = LongTermWindow.entries
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
                            LongTermWindow.WEEK -> "7D"
                            LongTermWindow.MONTH -> "30D"
                            LongTermWindow.ALL -> "All"
                        }
                    )
                }
            )
        }
    }
}
