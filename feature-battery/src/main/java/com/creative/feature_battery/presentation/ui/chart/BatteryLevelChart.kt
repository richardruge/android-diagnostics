package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.animation.core.snap
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
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun BatteryLevelChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    minX: Double? = null,
    maxX: Double? = null,
    minY: Double? = null,
    maxY: Double? = null,
    runAnimations: Boolean = true
) {
    val dateTimeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    val bottomAxisValueFormatter = remember {
        CartesianValueFormatter { _, x, _ ->
            dateTimeFormatter.format(x.roundToLong() * 1000)
        }
    }
    
    val startAxisValueFormatter = remember {
        CartesianValueFormatter { _, y, _ ->
            "${y.toInt()}%"
        }
    }

    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    
    val lineColor = Color(0xFF4CAF50) // Green for battery level
    val labelColor = MaterialTheme.colorScheme.onSurface

    val rangeProvider = remember {
        object : CartesianLayerRangeProvider {
            var minX: Double? = null
            var maxX: Double? = null
            var minY: Double? = null
            var maxY: Double? = null

            override fun getMinX(minX: Double, maxX: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore) = this.minX ?: minX
            override fun getMaxX(minX: Double, maxX: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore) = this.maxX ?: maxX
            override fun getMinY(minY: Double, maxY: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore) = this.minY ?: minY
            override fun getMaxY(minY: Double, maxY: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore) = this.maxY ?: maxY
        }
    }

    rangeProvider.minX = minX
    rangeProvider.maxX = maxX
    rangeProvider.minY = minY ?: 0.0
    rangeProvider.maxY = maxY ?: 100.0

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = remember(lineColor) {
            LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(Fill(lineColor.toArgb())),
                    areaFill = LineCartesianLayer.AreaFill.single(Fill(lineColor.copy(alpha = 0.2f).toArgb())),
                    pointConnector = LineCartesianLayer.PointConnector.cubic()
                )
            )
        },
        rangeProvider = rangeProvider
    )

    val chart = rememberCartesianChart(
        lineLayer,
        startAxis = VerticalAxis.rememberStart(
            label = rememberAxisLabelComponent(color = labelColor),
            valueFormatter = startAxisValueFormatter,
            guideline = null,
            tick = null
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(color = labelColor),
            valueFormatter = bottomAxisValueFormatter,
            labelRotationDegrees = 45f,
            guideline = null
        )
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        scrollState = scrollState,
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        animationSpec = if (runAnimations) null else snap()
    )
}
