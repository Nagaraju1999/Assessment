package com.nagaraju.stocktracker.feature.watchlist.presentation.state

import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.model.StockSearchResult

/**
 * Immutable snapshot of everything the watchlist screen needs to render.
 * The ViewModel exposes exactly one [WatchlistUiState] via [StateFlow] —
 * the screen never reads partial state from multiple sources.
 */
data class WatchlistUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSearchSheetVisible: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<StockSearchResult> = emptyList(),
    val isSearching: Boolean = false,
) {
    /** True when there is no watchlist data at all yet (first load, not a refresh). */
    val isEmpty: Boolean get() = stocks.isEmpty() && !isLoading && errorMessage == null
}
