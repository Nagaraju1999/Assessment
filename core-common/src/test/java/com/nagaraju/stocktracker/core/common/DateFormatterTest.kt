package com.nagaraju.stocktracker.core.common

import com.nagaraju.stocktracker.core.common.formatter.DateFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateFormatterTest {

    // A fixed, known instant. SimpleDateFormat is used to compute the
    // expected value with the same Locale.getDefault() the formatter
    // itself uses, avoiding a hardcoded timezone-dependent literal that
    // would fail in CI running a different default timezone than the
    // original author's machine.
    private val knownEpochSeconds = 1_718_461_800L

    @Test
    fun `toTime formats hours and minutes`() {
        val expected = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(knownEpochSeconds * 1000L))

        assertEquals(expected, DateFormatter.toTime(knownEpochSeconds))
    }

    @Test
    fun `toShortDate formats month and day`() {
        val expected = SimpleDateFormat("MMM dd", Locale.getDefault())
            .format(Date(knownEpochSeconds * 1000L))

        assertEquals(expected, DateFormatter.toShortDate(knownEpochSeconds))
    }

    @Test
    fun `toDateTime formats month day and time together`() {
        val expected = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            .format(Date(knownEpochSeconds * 1000L))

        assertEquals(expected, DateFormatter.toDateTime(knownEpochSeconds))
    }

    @Test
    fun `toFullDate formats month day and year`() {
        val expected = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(knownEpochSeconds * 1000L))

        assertEquals(expected, DateFormatter.toFullDate(knownEpochSeconds))
    }

    @Test
    fun `nowEpochSeconds returns a value close to the current system time`() {
        val before = System.currentTimeMillis() / 1000L
        val result = DateFormatter.nowEpochSeconds()
        val after = System.currentTimeMillis() / 1000L

        assertTrue(result in before..after)
    }
}
