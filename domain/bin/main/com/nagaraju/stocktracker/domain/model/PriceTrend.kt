package com.nagaraju.stocktracker.domain.model

/**
 * A lightweight trend indicator derived from a [StockHistory]'s closing
 * prices via simple linear regression — not a predictive ML model, but a
 * statistically grounded "is this trending up or down, and by roughly how
 * much" signal appropriate for a glance-level UI indicator.
 *
 * @param direction          Whether the regression line's slope is positive,
 *                            negative, or flat.
 * @param projectedNextClose The regression line's value one bar past the
 *                            last observed candle — a naive linear
 *                            projection, not a forecast guaranteed to be
 *                            accurate.
 * @param confidence         R-squared of the regression fit, in `0.0..1.0`.
 *                            Low confidence (a noisy, non-linear series)
 *                            is surfaced to the user rather than hidden, so
 *                            the indicator doesn't overstate its own
 *                            reliability.
 */
data class PriceTrend(
    val direction: TrendDirection,
    val projectedNextClose: Double,
    val confidence: Double,
)

enum class TrendDirection {
    UP,
    DOWN,
    FLAT,
}
