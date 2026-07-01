package com.nagaraju.stocktracker.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw JSON response from Finnhub GET /search.
 * The top-level object contains a [count] and a list of [results].
 *
 * API reference: https://finnhub.io/docs/api/symbol-search
 */
data class SearchResultDto(
    @SerializedName("count")  val count: Int,
    @SerializedName("result") val results: List<SearchItemDto>,
)

/**
 * A single symbol entry inside [SearchResultDto.results].
 */
data class SearchItemDto(
    /** Full company name. e.g. "Apple Inc" */
    @SerializedName("description") val description: String,
    /** Display symbol. e.g. "AAPL" */
    @SerializedName("displaySymbol") val displaySymbol: String,
    /** Exchange-qualified symbol. e.g. "AAPL" */
    @SerializedName("symbol") val symbol: String,
    /** Instrument type. e.g. "Common Stock" */
    @SerializedName("type") val type: String,
)
