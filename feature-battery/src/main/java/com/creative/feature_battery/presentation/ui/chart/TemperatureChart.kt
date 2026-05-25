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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import java.util.Locale
import kotlin.math.roundToInt

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
    val bottomAxisValueFormatter = remember(minX, maxX) {
        CartesianValueFormatter { _, x, _ ->
            if (x.isNaN() || maxX == null || minX == null) "" else {
                val rangeMinutes = (maxX - minX) / (60 * 1000)
                if (rangeMinutes > 90) { // 24h window (1440 mins)
                    val diffHours = (x - maxX) / (3600 * 1000)
                    "${diffHours.roundToInt()}"
                } else {
                    val diffMinutes = (x - maxX) / (60 * 1000)
                    "${diffMinutes.roundToInt()}"
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

    // Capture bounds to avoid shadowing in the anonymous object
    val fixedMinX = minX
    val fixedMaxX = maxX
    val fixedMinY = minY
    val fixedMaxY = maxY

    // Use a keyed range provider to ensure recomposition when bounds change
    val rangeProvider = remember(fixedMinX, fixedMaxX, fixedMinY, fixedMaxY) {
        object : CartesianLayerRangeProvider {
            override fun getMinX(minX: Double, maxX: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val fixed = fixedMinX
                if (fixed != null && fixed.isFinite()) {
                    return if (fixed == fixedMaxX) fixed - 1.0 else fixed
                }
                if (!minX.isFinite()) return 0.0
                return if (minX == maxX) minX - 1.0 else minX
            }

            override fun getMaxX(minX: Double, maxX: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val fixed = fixedMaxX
                if (fixed != null && fixed.isFinite()) {
                    return if (fixed == fixedMinX) fixed + 1.0 else fixed
                }
                if (!maxX.isFinite()) return 1.0
                return if (minX == maxX) maxX + 1.0 else maxX
            }

            override fun getMinY(minY: Double, maxY: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val fixed = fixedMinY
                if (fixed != null && fixed.isFinite()) {
                    return if (fixed == fixedMaxY) fixed - 1.0 else fixed
                }
                if (!minY.isFinite()) return 0.0
                return if (minY == maxY) minY - 1.0 else minY
            }

            override fun getMaxY(minY: Double, maxY: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val fixed = fixedMaxY
                if (fixed != null && fixed.isFinite()) {
                    return if (fixed == fixedMinY) fixed + 1.0 else fixed
                }
                if (!maxY.isFinite()) return 100.0
                return if (minY == maxY) maxY + 1.0 else maxY
            }
        }
    }

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
            guideline = null
        )
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        scrollState = scrollState,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        animationSpec = if (runAnimations) null else snap()
    )
}
