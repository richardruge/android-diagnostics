package com.creative.feature_network.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill

@Composable
fun SignalStrengthChart(
    history: List<Int>,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(history) {
        if (history.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(history.map { it.toDouble() })
                }
            }
        }
    }

    val startAxisValueFormatter = remember {
        CartesianValueFormatter { _, y, _ ->
            "${y.toInt()} dBm"
        }
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface
    val guidelineColor = MaterialTheme.colorScheme.outlineVariant

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(lineColor.toArgb())),
                    areaFill = LineCartesianLayer.AreaFill.single(Fill(lineColor.copy(alpha = 0.2f).toArgb()))
                )
            )
        ),
        startAxis = VerticalAxis.rememberStart(
            label = rememberAxisLabelComponent(color = labelColor),
            valueFormatter = startAxisValueFormatter,
            guideline = rememberLineComponent(fill = Fill(guidelineColor.toArgb()), thickness = 1.dp)
        )
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    )
}
