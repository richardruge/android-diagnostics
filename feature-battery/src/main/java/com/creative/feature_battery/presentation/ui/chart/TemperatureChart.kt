package com.creative.feature_battery.presentation.ui.chart

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
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
            dateTimeFormatter.format(x.roundToLong() * 1000)
        }
    }
    
    val startAxisValueFormatter = CartesianValueFormatter { _, y, _ ->
        "%.1f°C".format(Locale.US, y)
    }

    val scrollState = rememberVicoScrollState(scrollEnabled = false)

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            rangeProvider = remember(minX, maxX, minY, maxY) {
                CartesianLayerRangeProvider.fixed(
                    minX = minX,
                    maxX = maxX,
                    minY = minY,
                    maxY = maxY
                )
            }
        ),
        startAxis = VerticalAxis.rememberStart(
            valueFormatter = startAxisValueFormatter,
            title = "Temp"
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = bottomAxisValueFormatter,
            title = "Time",
            labelRotationDegrees = 45f
        )
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        scrollState = scrollState,
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        animationSpec = if (runAnimations) null else snap()
    )
}
