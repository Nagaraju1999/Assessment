package com.nagaraju.stocktracker.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw JSON response from Finnhub GET /stock/candle.
 *
 * Finnhub returns parallel arrays — each index i across all lists
 * represents one candle bar. The [status] field is "ok" on success
 * or "no_data" when the requested range has no trading data.
 *
 * API reference: https://finnhub.io/docs/api/stock-candles
 */
data class CandleDto(
    /** Closing prices */
    @SerializedName("c") val closePrices: List<Double>?,
    /** High prices */
    @SerializedName("h") val highPrices: List<Double>?,
    /** Low prices */
    @SerializedName("l") val lowPrices: List<Double>?,
    /** Opening prices */
    @SerializedName("o") val openPrices: List<Double>?,
    /** Volumes */
    @SerializedName("v") val volumes: List<Long>?,
    /** Unix timestamps (seconds) */
    @SerializedName("t") val timestamps: List<Long>?,
    /** "ok" or "no_data" */
    @SerializedName("s") val status: String?,
)
