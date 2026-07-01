package com.nagaraju.stocktracker.data

import com.nagaraju.stocktracker.core.database.entity.AlertConditionEntity
import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import com.nagaraju.stocktracker.data.mapper.toDomain
import com.nagaraju.stocktracker.data.mapper.toEntity
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertMapperTest {

    @Test
    fun `AlertEntity toDomain maps all fields including ABOVE condition`() {
        val entity = AlertEntity(
            id          = 1L,
            symbol      = "AAPL",
            condition   = AlertConditionEntity.ABOVE,
            targetPrice = 200.0,
            isEnabled   = true,
            isTriggered = false,
            createdAt   = 1000L,
        )

        val alert = entity.toDomain()

        assertEquals(1L,                      alert.id)
        assertEquals("AAPL",                  alert.symbol)
        assertEquals(AlertCondition.ABOVE,     alert.condition)
        assertEquals(200.0,                   alert.targetPrice, 0.001)
        assertEquals(true,                    alert.isEnabled)
        assertEquals(false,                   alert.isTriggered)
    }

    @Test
    fun `AlertEntity toDomain maps BELOW condition correctly`() {
        val entity = AlertEntity(
            symbol      = "TSLA",
            condition   = AlertConditionEntity.BELOW,
            targetPrice = 150.0,
            isEnabled   = true,
            isTriggered = false,
            createdAt   = 1000L,
        )

        assertEquals(AlertCondition.BELOW, entity.toDomain().condition)
    }

    @Test
    fun `StockAlert toEntity maps all fields including ABOVE condition`() {
        val alert = StockAlert(
            id          = 5L,
            symbol      = "AAPL",
            condition   = AlertCondition.ABOVE,
            targetPrice = 200.0,
            isEnabled   = true,
            isTriggered = false,
            createdAt   = 1000L,
        )

        val entity = alert.toEntity()

        assertEquals(5L,                          entity.id)
        assertEquals("AAPL",                       entity.symbol)
        assertEquals(AlertConditionEntity.ABOVE,    entity.condition)
        assertEquals(200.0,                        entity.targetPrice, 0.001)
    }

    @Test
    fun `round trip mapping preserves all fields`() {
        val original = StockAlert(
            id          = 9L,
            symbol      = "MSFT",
            condition   = AlertCondition.BELOW,
            targetPrice = 300.0,
            isEnabled   = false,
            isTriggered = true,
            createdAt   = 5000L,
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }
}
