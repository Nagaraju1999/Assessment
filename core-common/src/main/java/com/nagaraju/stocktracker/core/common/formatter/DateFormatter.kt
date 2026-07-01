package com.nagaraju.stocktracker.core.common.formatter

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Converts Unix epoch seconds (as returned by the Finnhub API) into
 * display-ready strings for chart axes, alert timestamps, and detail screens.
 *
 * [SimpleDateFormat] is intentionally used instead of [java.time] to stay
 * compatible with API 26+ without requiring desugaring configuration.
 */
object DateFormatter {

    private val timeFormat   = SimpleDateFormat("HH:mm",          Locale.getDefault())
    private val dateFormat   = SimpleDateFormat("MMM dd",         Locale.getDefault())
    private val fullFormat   = SimpleDateFormat("MMM dd, HH:mm",  Locale.getDefault())
    private val yearFormat   = SimpleDateFormat("MMM dd, yyyy",   Locale.getDefault())

    /** "14:30" — used on 1-day chart x-axis. */
    fun toTime(epochSeconds: Long): String =
        timeFormat.format(Date(epochSeconds * 1_000L))

    /** "Jun 12" — used on weekly / monthly chart x-axis. */
    fun toShortDate(epochSeconds: Long): String =
        dateFormat.format(Date(epochSeconds * 1_000L))

    /** "Jun 12, 14:30" — used on 3-month chart x-axis. */
    fun toDateTime(epochSeconds: Long): String =
        fullFormat.format(Date(epochSeconds * 1_000L))

    /** "Jun 12, 2024" — used for alert creation timestamps. */
    fun toFullDate(epochSeconds: Long): String =
        yearFormat.format(Date(epochSeconds * 1_000L))

    /** Returns the current epoch in seconds. */
    fun nowEpochSeconds(): Long = System.currentTimeMillis() / 1_000L
}
