package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.model.PriceTrendCalculator
import com.nagaraju.stocktracker.domain.model.StockCandle
import com.nagaraju.stocktracker.domain.model.TrendDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceTrendCalculatorTest {

    private fun candle(close: Double) = StockCandle(
        timestamp = 1000L, open = close, high = close, low = close, close = close, volume = 1000L,
    )

    @Test
    fun `returns null when fewer than 3 candles are provided`() {
        assertNull(PriceTrendCalculator.calculate(emptyList()))
        assertNull(PriceTrendCalculator.calculate(listOf(candle(100.0))))
        assertNull(PriceTrendCalculator.calculate(listOf(candle(100.0), candle(101.0))))
    }

    @Test
    fun `detects an UP trend for a steadily rising series`() {
        val candles = listOf(100.0, 102.0, 104.0, 106.0, 108.0).map { candle(it) }

        val trend = PriceTrendCalculator.calculate(candles)

        assertEquals(TrendDirection.UP, trend?.direction)
    }

    @Test
    fun `detects a DOWN trend for a steadily falling series`() {
        val candles = listOf(108.0, 106.0, 104.0, 102.0, 100.0).map { candle(it) }

        val trend = PriceTrendCalculator.calculate(candles)

        assertEquals(TrendDirection.DOWN, trend?.direction)
    }

    @Test
    fun `detects FLAT for an unchanging series`() {
        val candles = listOf(100.0, 100.0, 100.0, 100.0, 100.0).map { candle(it) }

        val trend = PriceTrendCalculator.calculate(candles)

        assertEquals(TrendDirection.FLAT, trend?.direction)
    }

    @Test
    fun `confidence is 1_0 for a perfectly linear series`() {
        val candles = listOf(100.0, 102.0, 104.0, 106.0, 108.0).map { candle(it) }

        val trend = PriceTrendCalculator.calculate(candles)

        assertEquals(1.0, trend!!.confidence, 0.0001)
    }

    @Test
    fun `confidence is 1_0 for a perfectly flat series`() {
        val candles = listOf(100.0, 100.0, 100.0, 100.0).map { candle(it) }

        val trend = PriceTrendCalculator.calculate(candles)

        assertEquals(1.0, trend!!.confidence, 0.0001)
    }

    @Test
    fun `confidence is lower for a noisy non-linear series`() {
        val noisy = listOf(100.0, 110.0, 95.0, 115.0, 90.0, 120.0).map { candle(it) }
        val clean = listOf(100.0, 104.0, 108.0, 112.0, 116.0, 120.0).map { candle(it) }

        val noisyTrend = PriceTrendCalculator.calculate(noisy)
        val cleanTrend = PriceTrendCalculator.calculate(clean)

        assertTrue(noisyTrend!!.confidence < cleanTrend!!.confidence)
    }

    @Test
    fun `projectedNextClose continues the linear pattern for a clean rising series`() {
        val candles = listOf(100.0, 102.0, 104.0, 106.0, 108.0).map { candle(it) }

        val trend = PriceTrendCalculator.calculate(candles)

        assertEquals(110.0, trend!!.projectedNextClose, 0.01)
    }

    @Test
    fun `confidence is always within 0 and 1`() {
        val candles = listOf(100.0, 50.0, 200.0, 10.0, 300.0).map { candle(it) }

        val trend = PriceTrendCalculator.calculate(candles)

        assertTrue(trend!!.confidence in 0.0..1.0)
    }
}
