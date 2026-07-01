package com.nagaraju.stocktracker.domain.model

/**
 * A single historical price bar used to render the stock details chart.
 *
 * @param timestamp Unix epoch seconds for this bar.
 * @param open      Opening price for the bar's interval.
 * @param high      Highest price during the interval.
 * @param low       Lowest price during the interval.
 * @param close     Closing price for the bar's interval.
 * @param volume    Shares traded during the interval.
 */
data class StockCandle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
)

/**
 * A complete set of historical candles for a symbol over a [TimeRange],
 * plus the derived high/low across the whole series — used to scale
 * the chart's Y-axis without re-scanning the list on every recomposition.
 */
data class StockHistory(
    val symbol: String,
    val range: TimeRange,
    val candles: List<StockCandle>,
) {
    val highestPrice: Double get() = candles.maxOfOrNull { it.high } ?: 0.0
    val lowestPrice: Double get() = candles.minOfOrNull { it.low } ?: 0.0
}

/**
 * Selectable time ranges on the stock details chart.
 *
 * [resolution] and [rangeSeconds] map directly to the Finnhub candle API
 * parameters, keeping the conversion logic in one place.
 */
enum class TimeRange(val label: String, val resolution: String, val rangeSeconds: Long) {
    ONE_DAY("1D", "D", 24 * 60 * 60L),
    ONE_WEEK("1W", "D", 7 * 24 * 60 * 60L),
    ONE_MONTH("1M", "D", 30 * 24 * 60 * 60L),
    THREE_MONTHS("3M", "D", 90 * 24 * 60 * 60L),
    ONE_YEAR("1Y", "W", 365 * 24 * 60 * 60L),
}
