package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.model.Stock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockTest {

    private fun stock(change: Double) = Stock(
        symbol         = "AAPL",
        companyName    = "Apple Inc",
        currentPrice   = 182.34,
        change         = change,
        percentChange  = 1.0,
        highPrice      = 184.0,
        lowPrice       = 180.0,
        openPrice      = 181.0,
        previousClose  = 181.11,
        addedAt        = 1000L,
    )

    @Test
    fun `isPositive is true when change is positive`() {
        assertTrue(stock(change = 1.5).isPositive)
    }

    @Test
    fun `isPositive is true when change is exactly zero`() {
        assertTrue(stock(change = 0.0).isPositive)
    }

    @Test
    fun `isPositive is false when change is negative`() {
        assertFalse(stock(change = -1.5).isPositive)
    }
}
