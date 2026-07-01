package com.nagaraju.stocktracker.feature.alerts

import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert
import com.nagaraju.stocktracker.feature.alerts.notification.buildNotificationContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertNotifierContentTest {

    private fun alert(condition: AlertCondition, targetPrice: Double = 200.0) = StockAlert(
        id = 1L, symbol = "AAPL", condition = condition, targetPrice = targetPrice,
        isEnabled = true, isTriggered = true, createdAt = 1000L,
    )

    @Test
    fun `ABOVE condition produces risen above wording`() {
        val content = buildNotificationContent(alert(AlertCondition.ABOVE, 200.0))

        assertEquals("AAPL price alert", content.title)
        assertTrue(content.body.contains("risen above"))
        assertTrue(content.body.contains("200.0"))
    }

    @Test
    fun `BELOW condition produces fallen below wording`() {
        val content = buildNotificationContent(alert(AlertCondition.BELOW, 150.0))

        assertTrue(content.body.contains("fallen below"))
        assertTrue(content.body.contains("150.0"))
    }

    @Test
    fun `title always includes the alert symbol`() {
        val content = buildNotificationContent(alert(AlertCondition.ABOVE))

        assertTrue(content.title.startsWith("AAPL"))
    }
}
