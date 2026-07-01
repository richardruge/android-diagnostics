package com.creative.feature_battery.presentation.ui.debug

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creative.feature_battery.domain.model.BatteryInfo
import com.creative.feature_battery.domain.repository.BatteryAggregation
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BatteryDebugScreen(
    viewModel: BatteryDebugViewModel = koinViewModel()
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val aggregations by viewModel.aggregations.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Raw Data", "Aggregated")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Battery History Debug",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (selectedTab == 0) "Total records: ${history.size}" else "Total buckets: ${aggregations.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { 
                    if (selectedTab == 0) viewModel.seedMockData() 
                    else viewModel.seedMockAggregations()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Seed Mock Data")
                }
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear History")
                }
            }
        }

        SecondaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (selectedTab == 0) {
            RawDataView(history)
        } else {
            AggregatedDataView(aggregations)
        }
    }
}

@Composable
private fun RawDataView(history: List<BatteryInfo>) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data collected yet", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val horizontalScrollState = rememberScrollState()
        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            RawTableHeader()
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(history.sortedByDescending { it.timestamp }) { info ->
                    RawTableRow(info)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AggregatedDataView(aggregations: List<BatteryAggregation>) {
    if (aggregations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No aggregated data yet", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val horizontalScrollState = rememberScrollState()
        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
            AggregatedTableHeader()
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(aggregations.sortedByDescending { it.timestamp }) { aggregation ->
                    AggregatedTableRow(aggregation)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RawTableHeader() {
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = "Time", width = 85.dp, isHeader = true)
        TableCell(text = "Lvl", width = 45.dp, isHeader = true)
        TableCell(text = "Temp", width = 55.dp, isHeader = true)
        TableCell(text = "Volt", width = 65.dp, isHeader = true)
        TableCell(text = "mA (Now)", width = 75.dp, isHeader = true)
        TableCell(text = "mA (Avg)", width = 75.dp, isHeader = true)
        TableCell(text = "Health", width = 65.dp, isHeader = true)
        TableCell(text = "Chg", width = 30.dp, isHeader = true)
    }
}

@Composable
private fun RawTableRow(info: BatteryInfo) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = dateFormat.format(Date(info.timestamp)), width = 85.dp)
        TableCell(text = "${info.level}%", width = 45.dp)
        TableCell(text = "${"%.1f".format(info.temperatureC)}°C", width = 55.dp)
        TableCell(text = info.voltageMv?.let { "${it}mV" } ?: "-", width = 65.dp)
        TableCell(text = info.currentNowMa?.let { "${it}mA" } ?: "-", width = 75.dp)
        TableCell(text = info.currentAverageMa?.let { "${it}mA" } ?: "-", width = 75.dp)
        TableCell(text = info.health.name, width = 75.dp)
        TableCell(text = if (info.isCharging) "Yes" else "No", width = 30.dp)
    }
}

@Composable
private fun AggregatedTableHeader() {
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = "Date/Time", width = 120.dp, isHeader = true)
        TableCell(text = "Avg Lvl", width = 65.dp, isHeader = true)
        TableCell(text = "Avg Temp", width = 75.dp, isHeader = true)
        TableCell(text = "Avg Volt", width = 75.dp, isHeader = true)
        TableCell(text = "Avg mA", width = 75.dp, isHeader = true)
        TableCell(text = "Dur (m)", width = 60.dp, isHeader = true)
    }
}

@Composable
private fun AggregatedTableRow(aggregation: BatteryAggregation) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(text = dateFormat.format(Date(aggregation.timestamp)), width = 120.dp)
        TableCell(text = "${"%.1f".format(aggregation.avgLevel)}%", width = 65.dp)
        TableCell(text = "${"%.1f".format(aggregation.avgTemperatureC)}°C", width = 75.dp)
        TableCell(text = aggregation.avgVoltageMv?.let { "${"%.0f".format(it)}mV" } ?: "-", width = 75.dp)
        TableCell(text = aggregation.avgCurrentMa?.let { "${"%.0f".format(it)}mA" } ?: "-", width = 75.dp)
        TableCell(text = "${aggregation.bucketDurationMinutes}", width = 60.dp)
    }
}

@Composable
private fun TableCell(
    text: String,
    width: Dp,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
}
