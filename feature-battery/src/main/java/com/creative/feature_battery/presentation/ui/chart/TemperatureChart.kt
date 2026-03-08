package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel

@Composable
fun TemperatureChart(temperatures: List<Float>) {
    if (temperatures.isEmpty()) return

    val model = remember<CartesianChartModel>(temperatures) {
        CartesianChartModel(
            listOf(
                LineCartesianLayerModel.build {
                    series(temperatures)
                }
            )
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
        ),
        model = model,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    )
}
