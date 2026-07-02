package com.creative.feature_battery.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.creative.feature_battery.presentation.AppDischargeUiModel
import com.creative.feature_battery.presentation.AppDischargeViewModel
import com.creative.feature_battery.presentation.AppTimeWindow
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit

@Composable
fun AppDischargeScreen(
    viewModel: AppDischargeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Window Selector
            SingleChoiceSegmentedButtonRow {
                AppTimeWindow.entries.forEachIndexed { index, window ->
                    SegmentedButton(
                        selected = uiState.selectedWindow == window,
                        onClick = { viewModel.setWindow(window) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTimeWindow.entries.size)
                    ) {
                        Text(window.label)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = !uiState.hasPermission) {
            PermissionWarning(onGrant = { viewModel.requestPermission(context) })
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Total Power Impact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "%.0f mAh".format(uiState.totalTrackedMah),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.sessions.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No sessions recorded yet. Switch apps to see impact.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.sessions) { session ->
                    AppSessionCard(session, uiState.maxMah)
                }
            }
        }
    }
}

@Composable
private fun PermissionWarning(onGrant: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Grant Usage Access to track apps.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onGrant, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Grant")
            }
        }
    }
}

@Composable
private fun AppSessionCard(session: AppDischargeUiModel, maxMah: Double) {
    val isHighDrain = session.drainRatePercentPerHour > 5.0
    val washColor = if (isHighDrain) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val fraction = (session.totalMah / maxMah).coerceIn(0.0, 1.0).toFloat()
                drawRect(
                    color = washColor.copy(alpha = 0.08f),
                    size = size.copy(width = size.width * fraction)
                )
            },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        if (session.icon != null) {
                            Image(
                                bitmap = session.icon.toBitmap().asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = session.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Tiny Sparkline
                        if (session.sparklinePoints.isNotEmpty()) {
                            Canvas(modifier = Modifier.size(width = 40.dp, height = 16.dp)) {
                                val points = session.sparklinePoints
                                val maxVal = points.maxOrNull()?.takeIf { it > 0 } ?: 1f
                                val cWidth = size.width
                                val cHeight = size.height
                                val path = Path().apply {
                                    points.forEachIndexed { i, v ->
                                        val px = (i.toFloat() / (points.size - 1)) * cWidth
                                        val py = cHeight - (v / maxVal) * cHeight
                                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = washColor.copy(alpha = 0.5f),
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                    }
                    Text(
                        text = "Used for ${formatDuration(session.durationMs)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%.1f%%".format(session.totalPercentage),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "%.0f mAh".format(session.totalMah),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val intensityColor = if (isHighDrain) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                Surface(
                    color = intensityColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = "%.1f%% / hr".format(session.drainRatePercentPerHour),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = intensityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isHighDrain) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Warning, 
                        contentDescription = "High Drain",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
