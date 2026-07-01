package com.nagaraju.stocktracker.feature.stockdetails.presentation.state

sealed interface StockDetailsEvent {
    data class ShowError(val message: String) : StockDetailsEvent
    data class AddedToWatchlist(val symbol: String) : StockDetailsEvent
    data class RemovedFromWatchlist(val symbol: String) : StockDetailsEvent
}
