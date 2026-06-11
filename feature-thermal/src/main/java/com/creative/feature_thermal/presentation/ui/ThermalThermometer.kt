package com.creative.feature_thermal.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.creative.core_model.ThermalSeverity

@Composable
fun ThermalThermometer(
    severity: ThermalSeverity,
    temperatureC: Float,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = when (severity) {
            ThermalSeverity.NORMAL -> Color(0xFF4CAF50)
            ThermalSeverity.WARM -> Color(0xFFFFC107)
            ThermalSeverity.HOT -> Color(0xFFFF9800)
            ThermalSeverity.CRITICAL -> Color(0xFFF44336)
        },
        label = "thermalColor"
    )

    val progress by animateFloatAsState(
        targetValue = when (severity) {
            ThermalSeverity.NORMAL -> 0.25f
            ThermalSeverity.WARM -> 0.5f
            ThermalSeverity.HOT -> 0.75f
            ThermalSeverity.CRITICAL -> 1f
        },
        label = "thermalProgress"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(color)
            )
            
            // Bulb
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .align(Alignment.BottomCenter)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${"%.1f".format(temperatureC)}°C",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = severity.name,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
