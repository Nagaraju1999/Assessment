package com.nagaraju.stocktracker.core.common

import com.nagaraju.stocktracker.core.common.extensions.toCompactNumber
import com.nagaraju.stocktracker.core.common.extensions.toFormattedChange
import com.nagaraju.stocktracker.core.common.extensions.toFormattedPrice
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberExtensionsTest {

    @Test
    fun `toFormattedPrice formats with dollar sign and two decimals`() {
        assertEquals("$182.34", 182.34.toFormattedPrice())
        assertEquals("$0.00",   0.0.toFormattedPrice())
        assertEquals("$1.10",   1.1.toFormattedPrice())
    }

    @Test
    fun `toFormattedChange shows plus sign for positive values`() {
        assertEquals("+1.45%",  1.45.toFormattedChange())
        assertEquals("+0.00%",  0.0.toFormattedChange())
    }

    @Test
    fun `toFormattedChange shows minus sign for negative values`() {
        assertEquals("-2.30%", (-2.3).toFormattedChange())
    }

    @Test
    fun `toCompactNumber formats billions`() {
        assertEquals("1.5B", 1_500_000_000L.toCompactNumber())
    }

    @Test
    fun `toCompactNumber formats millions`() {
        assertEquals("2.3M", 2_300_000L.toCompactNumber())
    }

    @Test
    fun `toCompactNumber formats thousands`() {
        assertEquals("4.5K", 4_500L.toCompactNumber())
    }

    @Test
    fun `toCompactNumber returns raw string for small values`() {
        assertEquals("999", 999L.toCompactNumber())
    }
}
