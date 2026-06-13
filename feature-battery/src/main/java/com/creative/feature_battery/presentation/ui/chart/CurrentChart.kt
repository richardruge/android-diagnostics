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
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CurrentChart(
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
            if (!x.isFinite() || maxX == null || minX == null || !maxX.isFinite() || !minX.isFinite()) "" else {
                val rangeMinutes = (maxX - minX) / (60 * 1000)
                if (rangeMinutes > 90) { // 24h window
                    val diffHours = (x - maxX) / (3600 * 1000)
                    if (diffHours.isFinite()) "${diffHours.roundToInt()}h" else ""
                } else {
                    val diffMinutes = (x - maxX) / (60 * 1000)
                    if (diffMinutes.isFinite()) "${diffMinutes.roundToInt()}m" else ""
                }
            }
        }
    }
    
    val startAxisValueFormatter = remember {
        CartesianValueFormatter { _, y, _ ->
            if (!y.isFinite()) "" else "%.0fmA".format(Locale.US, y)
        }
    }

    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    
    val lineColor = Color(0xFFFF9800) // Amber for current
    val labelColor = MaterialTheme.colorScheme.onSurface

    val fixedMinX = minX
    val fixedMaxX = maxX
    val fixedMinY = minY
    val fixedMaxY = maxY

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
                    return if (fixed == fixedMaxY) fixed - 100.0 else fixed
                }
                if (!minY.isFinite()) return -1000.0
                val effectiveMaxY = if (maxY.isFinite()) maxY else minY + 100.0
                return if (minY == effectiveMaxY) minY - 100.0 else minY
            }

            override fun getMaxY(minY: Double, maxY: Double, extraStore: com.patrykandpatrick.vico.core.common.data.ExtraStore): Double {
                val fixed = fixedMaxY
                if (fixed != null && fixed.isFinite()) {
                    return if (fixed == fixedMinY) fixed + 100.0 else fixed
                }
                if (!maxY.isFinite()) return 1000.0
                val effectiveMinY = if (minY.isFinite()) minY else maxY - 100.0
                return if (effectiveMinY == maxY) maxY + 100.0 else maxY
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
