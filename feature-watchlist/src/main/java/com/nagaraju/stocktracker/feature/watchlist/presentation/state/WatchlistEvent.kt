package com.nagaraju.stocktracker.feature.watchlist.presentation.state

/**
 * One-time events the screen should react to exactly once — a Snackbar
 * message, for instance, must not reappear on configuration change or
 * recomposition the way a [WatchlistUiState] field would. The ViewModel
 * exposes these via [kotlinx.coroutines.flow.SharedFlow] rather than
 * [kotlinx.coroutines.flow.StateFlow].
 */
sealed interface WatchlistEvent {
    data class ShowError(val message: String) : WatchlistEvent
    data class StockAdded(val symbol: String) : WatchlistEvent
    data class StockRemoved(val symbol: String) : WatchlistEvent
}
