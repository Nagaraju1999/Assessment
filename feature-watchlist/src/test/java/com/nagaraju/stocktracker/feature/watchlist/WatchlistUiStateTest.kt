package com.nagaraju.stocktracker.feature.watchlist

import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.feature.watchlist.presentation.state.WatchlistUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchlistUiStateTest {

    private fun stock() = Stock(
        symbol = "AAPL", companyName = "Apple Inc", currentPrice = 182.0,
        change = 1.0, percentChange = 0.5, highPrice = 184.0, lowPrice = 180.0,
        openPrice = 181.0, previousClose = 181.0, addedAt = 1000L,
    )

    @Test
    fun `isEmpty is true when no stocks, not loading, and no error`() {
        val state = WatchlistUiState(stocks = emptyList(), isLoading = false, errorMessage = null)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `isEmpty is false when stocks are present`() {
        val state = WatchlistUiState(stocks = listOf(stock()), isLoading = false, errorMessage = null)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `isEmpty is false while loading even with no stocks`() {
        val state = WatchlistUiState(stocks = emptyList(), isLoading = true, errorMessage = null)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `isEmpty is false when an error message is present`() {
        val state = WatchlistUiState(stocks = emptyList(), isLoading = false, errorMessage = "Network error")
        assertFalse(state.isEmpty)
    }
}
