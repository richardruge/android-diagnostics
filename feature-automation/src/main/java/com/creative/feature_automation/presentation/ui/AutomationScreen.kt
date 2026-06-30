package com.creative.feature_automation.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutomationScreen(viewModel: AutomationViewModel = koinViewModel()) {
    val ui = viewModel.state.collectAsState().value

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Automation Tasks", style = MaterialTheme.typography.headlineSmall)

        val tasks = listOf(
            "battery_optimize" to "Optimize Battery",
            "clear_cache" to "Clear Cache",
            "system_scan" to "System Scan",
            "failing_task" to "Run Faulty Task"
        )

        tasks.forEach { (id, label) ->
            Button(
                onClick = { viewModel.execute(id) },
                enabled = !ui.isRunning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label)
            }
        }

        if (ui.isRunning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text("Executing automation…")
        }

        ui.errorMessage?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }

        ui.resultMessage?.let {
            Text("Result: $it", color = MaterialTheme.colorScheme.primary)
        }
    }
}
