package com.creative.feature_battery.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedHealthIndicator(
    stateOfHealth: Int,
    cycleCount: Int?,
    modifier: Modifier = Modifier,
    segments: Int = 10
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Capacity Retention",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$stateOfHealth%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    stateOfHealth > 85 -> Color(0xFF4CAF50)
                    stateOfHealth > 75 -> Color(0xFFFFA000)
                    else -> Color(0xFFF44336)
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val activeSegments = (stateOfHealth / (100 / segments.toFloat())).toInt()
            
            for (i in 1..segments) {
                val color = when {
                    i <= activeSegments -> {
                        when {
                            stateOfHealth > 85 -> Color(0xFF4CAF50)
                            stateOfHealth > 75 -> Color(0xFFFFA000)
                            else -> Color(0xFFF44336)
                        }
                    }
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }

        if (cycleCount != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total Charge Cycles: $cycleCount",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
