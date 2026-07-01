package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockAlertTest {

    private fun alert(condition: AlertCondition, targetPrice: Double) = StockAlert(
        symbol      = "AAPL",
        condition   = condition,
        targetPrice = targetPrice,
        isEnabled   = true,
        isTriggered = false,
        createdAt   = 1000L,
    )

    @Test
    fun `ABOVE condition is satisfied when price equals target`() {
        val result = alert(AlertCondition.ABOVE, 200.0).isSatisfiedBy(200.0)
        assertTrue(result)
    }

    @Test
    fun `ABOVE condition is satisfied when price exceeds target`() {
        val result = alert(AlertCondition.ABOVE, 200.0).isSatisfiedBy(205.0)
        assertTrue(result)
    }

    @Test
    fun `ABOVE condition is not satisfied when price is below target`() {
        val result = alert(AlertCondition.ABOVE, 200.0).isSatisfiedBy(195.0)
        assertFalse(result)
    }

    @Test
    fun `BELOW condition is satisfied when price equals target`() {
        val result = alert(AlertCondition.BELOW, 150.0).isSatisfiedBy(150.0)
        assertTrue(result)
    }

    @Test
    fun `BELOW condition is satisfied when price is under target`() {
        val result = alert(AlertCondition.BELOW, 150.0).isSatisfiedBy(145.0)
        assertTrue(result)
    }

    @Test
    fun `BELOW condition is not satisfied when price is above target`() {
        val result = alert(AlertCondition.BELOW, 150.0).isSatisfiedBy(155.0)
        assertFalse(result)
    }
}
