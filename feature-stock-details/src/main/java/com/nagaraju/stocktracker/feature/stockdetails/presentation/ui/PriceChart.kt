package com.nagaraju.stocktracker.feature.stockdetails.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.nagaraju.stocktracker.domain.model.StockCandle

/**
 * Custom Canvas-drawn line chart plotting closing price over time.
 *
 * Built with [androidx.compose.foundation.Canvas] rather than a third-party
 * charting library — a single-series line chart is well within what Canvas
 * draws cleanly without added abstraction.
 *
 * @param candles    Historical price bars, oldest first.
 * @param lineColor  Stroke color — callers pass the gain/loss semantic
 *                    color so the chart visually matches the price chip.
 */
@Composable
fun PriceChart(
    candles: List<StockCandle>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    if (candles.isEmpty()) return

    val closes = candles.map { it.close }
    val maxPrice = closes.max()
    val minPrice = closes.min()
    // Guard against a flat series (all prices identical) producing a
    // division by zero when normalizing Y coordinates.
    val priceRange = (maxPrice - minPrice).takeIf { it > 0.0 } ?: 1.0

    val fillColor = lineColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        ) {
            val widthStep = size.width / (closes.size - 1).coerceAtLeast(1)

            fun yFor(price: Double): Float {
                val normalized = (price - minPrice) / priceRange
                // Canvas Y grows downward, so invert: higher price = smaller Y.
                return size.height - (normalized * size.height).toFloat()
            }

            val linePoints = closes.mapIndexed { index, price ->
                Offset(x = index * widthStep, y = yFor(price))
            }

            // Filled area under the line, for visual weight matching common
            // stock-app chart conventions (e.g. Robinhood, Google Finance).
            val fillPath = Path().apply {
                moveTo(linePoints.first().x, size.height)
                linePoints.forEach { point -> lineTo(point.x, point.y) }
                lineTo(linePoints.last().x, size.height)
                close()
            }
            drawPath(path = fillPath, color = fillColor)

            // The price line itself, drawn as connected segments.
            for (i in 0 until linePoints.size - 1) {
                drawLine(
                    color = lineColor,
                    start = linePoints[i],
                    end = linePoints[i + 1],
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

/**
 * Convenience overload that derives the line color from whether the series
 * trended up or down over its visible range, matching [PriceChangeChip]'s
 * gain/loss color convention.
 */
@Composable
fun PriceChart(candles: List<StockCandle>, modifier: Modifier = Modifier) {
    val isPositiveTrend = candles.isNotEmpty() && candles.last().close >= candles.first().close
    val color = if (isPositiveTrend) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }
    PriceChart(candles = candles, lineColor = color, modifier = modifier)
}
