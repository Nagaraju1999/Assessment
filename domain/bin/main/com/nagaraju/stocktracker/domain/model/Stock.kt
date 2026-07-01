package com.nagaraju.stocktracker.domain.model

/**
 * A stock the user is tracking, combining persisted watchlist metadata
 * with the latest live quote fetched from the network.
 *
 * This is the model the presentation layer consumes — it never sees
 * database entities or network DTOs directly.
 *
 * @param symbol         Exchange symbol. e.g. "AAPL"
 * @param companyName    Display name. e.g. "Apple Inc"
 * @param currentPrice   Latest traded price.
 * @param change         Absolute price change since previous close.
 * @param percentChange  Percentage change since previous close.
 * @param highPrice      Day's high.
 * @param lowPrice       Day's low.
 * @param openPrice      Day's open.
 * @param previousClose  Previous trading day's close.
 * @param addedAt        Unix epoch seconds when added to the watchlist.
 */
data class Stock(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val openPrice: Double,
    val previousClose: Double,
    val addedAt: Long,
    /**
     * `true` when [currentPrice]/[change]/[percentChange] came from the
     * local cache rather than a live network response — set only when a
     * poll fails and a previously cached quote exists. The UI uses this
     * to show a "last updated" indicator instead of presenting stale data
     * as if it were live.
     */
    val isCached: Boolean = false,
    /** Unix epoch seconds when the displayed price was fetched, live or cached. */
    val priceTimestamp: Long = addedAt,
) {
    /** `true` when the stock is up since the previous close. */
    val isPositive: Boolean get() = change >= 0
}
