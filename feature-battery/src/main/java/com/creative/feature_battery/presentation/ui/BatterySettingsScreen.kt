package com.creative.feature_battery.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creative.feature_battery.presentation.BatterySettingsViewModel
import com.creative.feature_battery.presentation.BatterySettingsUiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatterySettingsScreen(
    onBack: () -> Unit,
    viewModel: BatterySettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "History Retention",
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = "Choose how long to keep the aggregated battery history. Older data will be automatically deleted.",
                style = MaterialTheme.typography.bodyMedium
            )

            val options = listOf(1, 3, 6, 12)
            options.forEach { months ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.retentionMonths == months,
                        onClick = { viewModel.updateRetentionPeriod(months) }
                    )
                    Text(
                        text = if (months == 1) "1 Month" else "$months Months",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            HorizontalDivider()

            Text(
                text = "Estimated Storage Requirement",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Based on your selection, the history data will take approximately:",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = "%.2f KB".format(uiState.estimatedStorageKb),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
