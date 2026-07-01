package com.nagaraju.stocktracker.feature.stockdetails

import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.feature.stockdetails.presentation.state.StockDetailsUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockDetailsUiStateTest {

    @Test
    fun `isChartEmpty is true when history has zero candles and is not loading`() {
        val state = StockDetailsUiState(
            history = StockHistory(symbol = "AAPL", range = TimeRange.ONE_DAY, candles = emptyList()),
            isHistoryLoading = false,
            historyErrorMessage = null,
        )

        assertTrue(state.isChartEmpty)
    }

    @Test
    fun `isChartEmpty is false while history is still loading`() {
        val state = StockDetailsUiState(
            history = StockHistory(symbol = "AAPL", range = TimeRange.ONE_DAY, candles = emptyList()),
            isHistoryLoading = true,
        )

        assertFalse(state.isChartEmpty)
    }

    @Test
    fun `isChartEmpty is false when history is null`() {
        val state = StockDetailsUiState(history = null, isHistoryLoading = false)

        assertFalse(state.isChartEmpty)
    }

    @Test
    fun `isChartEmpty is false when an error message is present`() {
        val state = StockDetailsUiState(
            history = StockHistory(symbol = "AAPL", range = TimeRange.ONE_DAY, candles = emptyList()),
            isHistoryLoading = false,
            historyErrorMessage = "Failed to load",
        )

        assertFalse(state.isChartEmpty)
    }
}
