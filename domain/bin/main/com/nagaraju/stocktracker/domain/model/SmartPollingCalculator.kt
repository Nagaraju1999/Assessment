package com.nagaraju.stocktracker.domain.model

/**
 * Computes an adaptive polling interval for a single [StockAlert] based on
 * how close [currentPrice] is to its [StockAlert.targetPrice].
 *
 * This is the core of the smart-polling strategy: an alert whose price is
 * far from its threshold is checked infrequently, and polling tightens
 * automatically as price approaches the target — without the caller (the
 * repository) needing to track any state beyond the last known price.
 *
 * Pure, deterministic, and Android-free, so it's directly unit-testable.
 */
object SmartPollingCalculator {

    /** Longest interval used when price is far from the target (5 minutes). */
    const val MAX_INTERVAL_MS = 300_000L

    /** Shortest interval used when price is at or very near the target (15 seconds). */
    const val MIN_INTERVAL_MS = 15_000L

    /**
     * Distance from target, as a fraction of the target price, at or beyond
     * which an alert is considered "far" and polled at [MAX_INTERVAL_MS].
     * 5% — close enough to be a meaningful "nearby" threshold for typical
     * stock price volatility without polling tightly on every alert at
     * all times.
     */
    private const val FAR_THRESHOLD_FRACTION = 0.05

    /**
     * Returns the poll interval, in milliseconds, [alert] should next be
     * checked at, given its [currentPrice] (the most recently known price
     * for that symbol — may be slightly stale between polls, which is
     * acceptable since this only controls polling *frequency*, not the
     * trigger decision itself).
     *
     * Interpolates linearly between [MIN_INTERVAL_MS] (at or past the
     * target) and [MAX_INTERVAL_MS] (at or beyond [FAR_THRESHOLD_FRACTION]
     * away from the target).
     */
    fun nextIntervalMillis(alert: StockAlert, currentPrice: Double): Long {
        if (alert.targetPrice <= 0.0) return MAX_INTERVAL_MS

        val distanceFraction = kotlin.math.abs(currentPrice - alert.targetPrice) / alert.targetPrice
        if (distanceFraction >= FAR_THRESHOLD_FRACTION) return MAX_INTERVAL_MS

        val proximity = (distanceFraction / FAR_THRESHOLD_FRACTION).coerceIn(0.0, 1.0)
        return (MIN_INTERVAL_MS + proximity * (MAX_INTERVAL_MS - MIN_INTERVAL_MS)).toLong()
    }
}
