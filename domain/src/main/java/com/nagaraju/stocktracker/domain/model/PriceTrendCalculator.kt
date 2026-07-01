package com.nagaraju.stocktracker.domain.model

/**
 * Computes a [PriceTrend] from a list of closing prices via ordinary
 * least-squares linear regression, treating each candle's index as the
 * X-axis (evenly spaced, ignoring actual elapsed time between bars — a
 * reasonable simplification at the time scales this app's [TimeRange]
 * options use).
 *
 * Deliberately simple and dependency-free: this is presented to the user
 * as a directional trend indicator, not a financial forecast, and the
 * implementation is sized to match that — a handful of summation formulas,
 * no external statistics or ML library.
 */
object PriceTrendCalculator {

    /** Below this many candles, a regression line isn't meaningful. */
    private const val MIN_CANDLES_FOR_TREND = 3

    /**
     * Slope magnitude, as a fraction of the mean price, below which the
     * trend is reported as [TrendDirection.FLAT] rather than a barely-up
     * or barely-down direction that would be noise at typical stock
     * price volatility.
     */
    private const val FLAT_SLOPE_THRESHOLD_FRACTION = 0.0005

    /**
     * Returns `null` when there isn't enough data to compute a meaningful
     * trend (fewer than [MIN_CANDLES_FOR_TREND] candles) — callers treat a
     * `null` result the same as "don't show the indicator," rather than
     * showing a misleading trend computed from too few points.
     */
    fun calculate(candles: List<StockCandle>): PriceTrend? {
        if (candles.size < MIN_CANDLES_FOR_TREND) return null

        val n = candles.size
        val xValues = (0 until n).map { it.toDouble() }
        val yValues = candles.map { it.close }

        val xMean = xValues.average()
        val yMean = yValues.average()

        var numerator = 0.0
        var denominator = 0.0
        for (i in 0 until n) {
            val xDiff = xValues[i] - xMean
            numerator += xDiff * (yValues[i] - yMean)
            denominator += xDiff * xDiff
        }

        // denominator is only zero if every x value were identical, which
        // can't happen since x ranges 0..n-1 and n >= MIN_CANDLES_FOR_TREND.
        val slope = numerator / denominator
        val intercept = yMean - slope * xMean

        val projectedNextClose = intercept + slope * n

        val confidence = calculateRSquared(xValues, yValues, slope, intercept, yMean)

        val relativeSlope = slope / yMean
        val direction = when {
            kotlin.math.abs(relativeSlope) < FLAT_SLOPE_THRESHOLD_FRACTION -> TrendDirection.FLAT
            slope > 0 -> TrendDirection.UP
            else -> TrendDirection.DOWN
        }

        return PriceTrend(
            direction = direction,
            projectedNextClose = projectedNextClose,
            confidence = confidence,
        )
    }

    /**
     * R-squared: the fraction of variance in [yValues] explained by the
     * regression line. `1.0` means the points lie exactly on the line;
     * `0.0` means the line explains none of the variance.
     */
    private fun calculateRSquared(
        xValues: List<Double>,
        yValues: List<Double>,
        slope: Double,
        intercept: Double,
        yMean: Double,
    ): Double {
        var ssTotal = 0.0
        var ssResidual = 0.0
        for (i in xValues.indices) {
            val predicted = intercept + slope * xValues[i]
            ssTotal += (yValues[i] - yMean) * (yValues[i] - yMean)
            ssResidual += (yValues[i] - predicted) * (yValues[i] - predicted)
        }
        // A perfectly flat series has ssTotal == 0 — treat that as a
        // perfect fit (the "line" exactly matches every flat point)
        // rather than producing a division-by-zero NaN.
        if (ssTotal == 0.0) return 1.0
        return (1.0 - (ssResidual / ssTotal)).coerceIn(0.0, 1.0)
    }
}
