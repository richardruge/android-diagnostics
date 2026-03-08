package com.creative.feature_battery.presentation.ui.chart

import com.creative.feature_battery.domain.model.BatteryInfo

fun List<BatteryInfo>.toValues(): List<Float> =
    map { it.temperatureC }

fun List<BatteryInfo>.toMovingAverage(window: Int = 5): List<Float> {
    if (isEmpty()) return emptyList()

    val values = map { it.temperatureC }
    return values.mapIndexed { i, _ ->
        val start = maxOf(0, i - window + 1)
        val slice = values.subList(start, i + 1)
        slice.average().toFloat()
    }
}
