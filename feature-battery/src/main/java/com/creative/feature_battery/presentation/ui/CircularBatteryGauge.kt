package com.creative.feature_battery.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularBatteryGauge(
    level: Int,
    health: Int?, // Changed to nullable
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 12.dp
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(1000),
        label = "level"
    )
    
    val animatedHealth by animateFloatAsState(
        targetValue = (health ?: 100).toFloat(), // Animate to 100 if null for a clean look
        animationSpec = tween(1000),
        label = "health"
    )

    val isHealthAvailable = health != null

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            // Background track for Health
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Health (SoH) Outer Track
            if (isHealthAvailable) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to Color(0xFF9E9E9E),
                        1f to Color(0xFFE0E0E0)
                    ),
                    startAngle = 135f,
                    sweepAngle = (animatedHealth / 100f) * 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }

            // Background track for Level
            drawArc(
                color = Color.LightGray.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = (strokeWidth.toPx() * 0.6f), cap = StrokeCap.Round),
                size = this.size.copy(
                    width = this.size.width - strokeWidth.toPx() * 2.5f,
                    height = this.size.height - strokeWidth.toPx() * 2.5f
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    strokeWidth.toPx() * 1.25f,
                    strokeWidth.toPx() * 1.25f
                )
            )

            // Level Inner Track - Green to Red
            val levelColor = when {
                animatedLevel > 60 -> Color(0xFF4CAF50)
                animatedLevel > 20 -> Color(0xFFFFA000)
                else -> Color(0xFFF44336)
            }

            drawArc(
                color = levelColor,
                startAngle = 135f,
                sweepAngle = (animatedLevel / 100f) * 270f,
                useCenter = false,
                style = Stroke(width = (strokeWidth.toPx() * 0.6f), cap = StrokeCap.Round),
                size = this.size.copy(
                    width = this.size.width - strokeWidth.toPx() * 2.5f,
                    height = this.size.height - strokeWidth.toPx() * 2.5f
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    strokeWidth.toPx() * 1.25f,
                    strokeWidth.toPx() * 1.25f
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${level}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isHealthAvailable) "Health: ${health}%" else "Health: N/A",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
