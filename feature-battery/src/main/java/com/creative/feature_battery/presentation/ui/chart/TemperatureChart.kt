package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
fun TemperatureChart(
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
            if (x.isNaN()) "" else {
                try {
                    dateTimeFormatter.format(x.roundToLong() * 1000)
                } catch (e: Exception) {
                    ""
                }
            }
        }
    }
    
    val startAxisValueFormatter = remember {
        CartesianValueFormatter { _, y, _ ->
            if (y.isNaN()) "" else "%.1f°".format(Locale.US, y)
        }
    }

    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    
    val lineColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface

    // Use a stable range provider to avoid re-creating it every second
    val rangeProvider = remember {
        object : CartesianLayerRangeProvider {
            var minX: Double? = null
            var maxX: Double? = null
            var minY: Double? = null
            var maxY: Double? = null

            override fun getMinX(minX: Double, maxX: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val start = (this.minX ?: minX).takeUnless { it.isNaN() } ?: 0.0
                val end = (this.maxX ?: maxX).takeUnless { it.isNaN() } ?: (start + 1.0)
                return if (start == end) start - 1.0 else start
            }

            override fun getMaxX(minX: Double, maxX: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val start = (this.minX ?: minX).takeUnless { it.isNaN() } ?: 0.0
                val end = (this.maxX ?: maxX).takeUnless { it.isNaN() } ?: (start + 1.0)
                return if (start == end) end + 1.0 else end
            }

            override fun getMinY(minY: Double, maxY: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val start = (this.minY ?: minY).takeUnless { it.isNaN() } ?: 0.0
                val end = (this.maxY ?: maxY).takeUnless { it.isNaN() } ?: (start + 1.0)
                return if (start == end) start - 1.0 else start
            }

            override fun getMaxY(minY: Double, maxY: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val start = (this.minY ?: minY).takeUnless { it.isNaN() } ?: 0.0
                val end = (this.maxY ?: maxY).takeUnless { it.isNaN() } ?: (start + 1.0)
                return if (start == end) end + 1.0 else end
            }
        }
    }

    // Update the range provider's values without triggering a recomposition of the whole chart layer
    rangeProvider.minX = minX
    rangeProvider.maxX = maxX
    rangeProvider.minY = minY
    rangeProvider.maxY = maxY

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
