package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.SmartPollingCalculator
import com.nagaraju.stocktracker.domain.model.StockAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartPollingCalculatorTest {

    private fun alert(targetPrice: Double) = StockAlert(
        symbol = "AAPL", condition = AlertCondition.ABOVE, targetPrice = targetPrice,
        isEnabled = true, isTriggered = false, createdAt = 1000L,
    )

    @Test
    fun `interval is MIN when price exactly equals target`() {
        val interval = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 200.0)

        assertEquals(SmartPollingCalculator.MIN_INTERVAL_MS, interval)
    }

    @Test
    fun `interval is MAX when price is far above the 5 percent threshold`() {
        val interval = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 250.0)

        assertEquals(SmartPollingCalculator.MAX_INTERVAL_MS, interval)
    }

    @Test
    fun `interval is MAX exactly at the 5 percent boundary`() {
        val interval = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 210.0)

        assertEquals(SmartPollingCalculator.MAX_INTERVAL_MS, interval)
    }

    @Test
    fun `interval is between MIN and MAX partway through the threshold`() {
        val interval = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 204.0)

        assertTrue(interval > SmartPollingCalculator.MIN_INTERVAL_MS)
        assertTrue(interval < SmartPollingCalculator.MAX_INTERVAL_MS)
    }

    @Test
    fun `interval shrinks monotonically as price approaches target`() {
        val far = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 208.0)
        val nearer = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 204.0)
        val nearest = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 201.0)

        assertTrue(far >= nearer)
        assertTrue(nearer >= nearest)
    }

    @Test
    fun `works symmetrically for price below target`() {
        val above = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 202.0)
        val below = SmartPollingCalculator.nextIntervalMillis(alert(200.0), currentPrice = 198.0)

        assertEquals(above, below)
    }

    @Test
    fun `falls back to MAX when target price is zero or negative`() {
        assertEquals(SmartPollingCalculator.MAX_INTERVAL_MS, SmartPollingCalculator.nextIntervalMillis(alert(0.0), 50.0))
        assertEquals(SmartPollingCalculator.MAX_INTERVAL_MS, SmartPollingCalculator.nextIntervalMillis(alert(-10.0), 50.0))
    }

    @Test
    fun `MIN is strictly less than MAX`() {
        assertTrue(SmartPollingCalculator.MIN_INTERVAL_MS < SmartPollingCalculator.MAX_INTERVAL_MS)
    }
}
