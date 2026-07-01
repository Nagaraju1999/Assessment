package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.model.StockCandle
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.TimeRange
import org.junit.Assert.assertEquals
import org.junit.Test

class StockHistoryTest {

    private fun candle(high: Double, low: Double) = StockCandle(
        timestamp = 1000L,
        open      = (high + low) / 2,
        high      = high,
        low       = low,
        close     = (high + low) / 2,
        volume    = 1000L,
    )

    @Test
    fun `highestPrice returns the max high across all candles`() {
        val history = StockHistory(
            symbol  = "AAPL",
            range   = TimeRange.ONE_DAY,
            candles = listOf(
                candle(high = 180.0, low = 175.0),
                candle(high = 190.0, low = 178.0),
                candle(high = 185.0, low = 176.0),
            ),
        )

        assertEquals(190.0, history.highestPrice, 0.001)
    }

    @Test
    fun `lowestPrice returns the min low across all candles`() {
        val history = StockHistory(
            symbol  = "AAPL",
            range   = TimeRange.ONE_DAY,
            candles = listOf(
                candle(high = 180.0, low = 175.0),
                candle(high = 190.0, low = 178.0),
                candle(high = 185.0, low = 170.0),
            ),
        )

        assertEquals(170.0, history.lowestPrice, 0.001)
    }

    @Test
    fun `highestPrice and lowestPrice default to zero for empty candle list`() {
        val history = StockHistory(symbol = "AAPL", range = TimeRange.ONE_DAY, candles = emptyList())

        assertEquals(0.0, history.highestPrice, 0.001)
        assertEquals(0.0, history.lowestPrice, 0.001)
    }
}
