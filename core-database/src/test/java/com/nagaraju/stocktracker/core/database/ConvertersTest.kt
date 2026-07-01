package com.nagaraju.stocktracker.core.database

import com.nagaraju.stocktracker.core.database.converter.Converters
import com.nagaraju.stocktracker.core.database.entity.AlertConditionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromAlertCondition converts ABOVE to its string name`() {
        assertEquals("ABOVE", converters.fromAlertCondition(AlertConditionEntity.ABOVE))
    }

    @Test
    fun `fromAlertCondition converts BELOW to its string name`() {
        assertEquals("BELOW", converters.fromAlertCondition(AlertConditionEntity.BELOW))
    }

    @Test
    fun `toAlertCondition converts string back to ABOVE`() {
        assertEquals(AlertConditionEntity.ABOVE, converters.toAlertCondition("ABOVE"))
    }

    @Test
    fun `toAlertCondition converts string back to BELOW`() {
        assertEquals(AlertConditionEntity.BELOW, converters.toAlertCondition("BELOW"))
    }

    @Test
    fun `round trip conversion preserves the original value`() {
        AlertConditionEntity.entries.forEach { original ->
            val stringValue = converters.fromAlertCondition(original)
            val roundTripped = converters.toAlertCondition(stringValue)
            assertEquals(original, roundTripped)
        }
    }
}
