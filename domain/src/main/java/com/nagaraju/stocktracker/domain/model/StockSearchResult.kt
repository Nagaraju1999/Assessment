package com.nagaraju.stocktracker.domain.model

/**
 * A single symbol search result, used when the user searches for a stock
 * to add to their watchlist.
 */
data class StockSearchResult(
    val symbol: String,
    val displaySymbol: String,
    val description: String,
    val type: String,
)
