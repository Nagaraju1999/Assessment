package com.nagaraju.stocktracker.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw JSON response from Finnhub GET /quote.
 * Field names match the Finnhub API exactly via [SerializedName].
 * No logic lives here — this is a pure data carrier.
 *
 * API reference: https://finnhub.io/docs/api/quote
 */
data class QuoteDto(
    /** Current price */
    @SerializedName("c") val currentPrice: Double,
    /** Change (current - previous close) */
    @SerializedName("d") val change: Double?,
    /** Percent change */
    @SerializedName("dp") val percentChange: Double?,
    /** High price of the day */
    @SerializedName("h") val highPrice: Double,
    /** Low price of the day */
    @SerializedName("l") val lowPrice: Double,
    /** Open price of the day */
    @SerializedName("o") val openPrice: Double,
    /** Previous close price */
    @SerializedName("pc") val previousClose: Double,
    /** Unix timestamp of the last trade */
    @SerializedName("t") val timestamp: Long,
)
