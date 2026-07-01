package com.nagaraju.stocktracker.core.common.extensions

import java.util.Locale

/** Formats a price to two decimal places with a leading "$". e.g. "$182.34" */
fun Double.toFormattedPrice(): String = "$%.2f".format(this)

/**
 * Formats a percentage change with a leading sign and two decimal places.
 * e.g.  1.45 → "+1.45%"
 *       -2.3 → "-2.30%"
 */
fun Double.toFormattedChange(): String =
    if (this >= 0) "+%.2f%%".format(this) else "%.2f%%".format(this)

/**
 * Formats a large number (volume, market cap) with "K", "M", or "B" suffix.
 * e.g.  1_500_000 → "1.5M"
 */
fun Long.toCompactNumber(): String = when {
    this >= 1_000_000_000L -> "%.1fB".format(this / 1_000_000_000.0)
    this >= 1_000_000L     -> "%.1fM".format(this / 1_000_000.0)
    this >= 1_000L         -> "%.1fK".format(this / 1_000.0)
    else                   -> toString()
}
