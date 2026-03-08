package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private const val GREEN_MAX = 35f
private const val YELLOW_MAX = 42f

fun severityColor(temp: Float): Int =
    when {
        temp < GREEN_MAX -> Color(0xFF28A745).toArgb()
        temp < YELLOW_MAX -> Color(0xFFFFC107).toArgb()
        else -> Color(0xFFDC3545).toArgb()
    }
