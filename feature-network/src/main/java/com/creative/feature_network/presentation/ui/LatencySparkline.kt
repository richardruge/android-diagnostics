package com.creative.feature_network.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LatencySparkline(
    history: List<Long>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF4CAF50)
) {
    if (history.size < 2) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxPing = (history.maxOrNull() ?: 100).toFloat().coerceAtLeast(100f)
        val minPing = (history.minOrNull() ?: 0).toFloat()
        val range = (maxPing - minPing).coerceAtLeast(1f)
        
        val width = size.width
        val height = size.height
        val stepX = width / (history.size - 1)
        
        val path = Path().apply {
            history.forEachIndexed { index, ping ->
                val x = index * stepX
                // Higher ping = lower on screen (y increases downwards)
                // Normalize: 0 is maxPing, height is minPing (reversed for visual intuition)
                val y = height - ((ping - minPing) / range * height)
                
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
