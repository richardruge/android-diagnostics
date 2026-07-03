package com.creative.feature_battery.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creative.feature_battery.presentation.BatterySettingsUiState
import com.creative.feature_battery.presentation.BatterySettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatterySettingsScreen(
    onBack: () -> Unit,
    viewModel: BatterySettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BatterySettingsContent(
        uiState = uiState,
        onBack = onBack,
        onUpdateRetention = viewModel::updateRetentionPeriod,
        onUpdateIgnoreSystemProcesses = viewModel::updateIgnoreSystemProcesses
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatterySettingsContent(
    uiState: BatterySettingsUiState,
    onBack: () -> Unit,
    onUpdateRetention: (Int) -> Unit,
    onUpdateIgnoreSystemProcesses: (Boolean) -> Unit
) {
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.retentionMonths == months,
                        onClick = { onUpdateRetention(months) }
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

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ignore System Processes",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Exclude Android system processes and pre-installed apps from power impact tracking.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = uiState.ignoreSystemProcesses,
                    onCheckedChange = { onUpdateIgnoreSystemProcesses(it) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatterySettingsScreenPreview() {
    MaterialTheme {
        BatterySettingsContent(
            uiState = BatterySettingsUiState(
                retentionMonths = 6,
                estimatedStorageKb = 124.5,
                ignoreSystemProcesses = true
            ),
            onBack = {},
            onUpdateRetention = {},
            onUpdateIgnoreSystemProcesses = {}
        )
    }
}
