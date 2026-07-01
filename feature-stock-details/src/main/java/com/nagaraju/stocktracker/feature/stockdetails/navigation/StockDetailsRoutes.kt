package com.nagaraju.stocktracker.feature.stockdetails.navigation

import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink

/**
 * Route and argument contract for the stock details feature.
 *
 * The symbol is passed as a required path argument (not a query param)
 * since every navigation into this screen requires a symbol — there is
 * no valid "details screen with no stock" state.
 */
object StockDetailsRoutes {
    const val SYMBOL_ARG = "symbol"
    const val STOCK_DETAILS_PATTERN = "stock_details/{$SYMBOL_ARG}"
    private const val DEEP_LINK_URI = "stocktracker://stock/{$SYMBOL_ARG}"

    /** Builds a concrete route string for navigating to a specific [symbol]. */
    fun createRoute(symbol: String) = "stock_details/$symbol"

    /**
     * Allows the stock details screen to be opened directly via
     * `stocktracker://stock/AAPL`, e.g. from a system notification tap
     * when a price alert triggers.
     */
    fun deepLinks(): List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = DEEP_LINK_URI },
    )
}
