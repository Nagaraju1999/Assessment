package com.nagaraju.stocktracker.feature.watchlist.navigation

/**
 * Route constants for the watchlist feature, kept as plain string constants
 * (rather than a sealed class hierarchy) since this feature has a single
 * destination with no arguments — a sealed class would add indirection
 * without a corresponding benefit.
 */
object WatchlistRoutes {
    const val WATCHLIST = "watchlist"
}
