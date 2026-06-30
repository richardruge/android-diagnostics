package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LongTermBatteryChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    valueSuffix: String = "",
    isVoltage: Boolean = false
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    
    val bottomAxisValueFormatter = remember {
        CartesianValueFormatter { _, x, _ ->
            if (!x.isFinite()) "" else dateFormat.format(Date(x.toLong()))
        }
    }
    
    val startAxisValueFormatter = remember {
        CartesianValueFormatter { _, y, _ ->
            if (!y.isFinite()) "" else {
                if (isVoltage) "%.2fV".format(Locale.US, y / 1000f)
                else "${y.toInt()}$valueSuffix"
            }
        }
    }

    val scrollState = rememberVicoScrollState(scrollEnabled = true)
    
    val labelColor = MaterialTheme.colorScheme.onSurface
    val guidelineColor = MaterialTheme.colorScheme.outlineVariant

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = remember(lineColor) {
            LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(lineColor.toArgb())),
                    areaFill = LineCartesianLayer.AreaFill.single(Fill(lineColor.copy(alpha = 0.2f).toArgb())),
                    pointConnector = LineCartesianLayer.PointConnector.cubic()
                )
            )
        }
    )

    val chart = rememberCartesianChart(
        lineLayer,
        startAxis = VerticalAxis.rememberStart(
            label = rememberAxisLabelComponent(color = labelColor),
            valueFormatter = startAxisValueFormatter,
            guideline = rememberLineComponent(fill = Fill(guidelineColor.toArgb()), thickness = 1.dp)
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(color = labelColor),
            valueFormatter = bottomAxisValueFormatter,
            guideline = rememberLineComponent(fill = Fill(guidelineColor.toArgb()), thickness = 1.dp)
        )
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        scrollState = scrollState,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}
