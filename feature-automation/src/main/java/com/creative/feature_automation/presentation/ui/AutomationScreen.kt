package com.creative.feature_automation.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.Text

@Composable
fun AutomationScreen(viewModel: AutomationViewModel = koinViewModel()) {
    val ui = viewModel.state.collectAsState().value

    Column {
        Button(
            onClick = { viewModel.execute("task1") },
            enabled = !ui.isRunning
        ) {
            Text(if (ui.isRunning) "Running…" else "Run Automation")
        }

        when {
            ui.isRunning ->
                Text("Executing automation…")

            ui.errorMessage != null ->
                Text("Error: ${ui.errorMessage}")

            ui.resultMessage != null ->
                Text("Result: ${ui.resultMessage}")
        }
    }
}