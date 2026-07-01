package com.nagaraju.stocktracker.feature.watchlist.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nagaraju.stocktracker.feature.watchlist.presentation.ui.WatchlistRoute

/**
 * Registers the watchlist destination on the host [NavGraphBuilder].
 *
 * @param onStockClick       Invoked with the clicked stock's symbol so the
 *                             caller (app's `AppNavHost`) can navigate into
 *                             the stock details destination.
 * @param onNavigateToAlerts Invoked when the user taps the alerts icon.
 */
fun NavGraphBuilder.watchlistRoute(
    onStockClick: (symbol: String) -> Unit,
    onNavigateToAlerts: () -> Unit,
) {
    composable(route = WatchlistRoutes.WATCHLIST) {
        WatchlistRoute(
            onStockClick = onStockClick,
            onNavigateToAlerts = onNavigateToAlerts,
        )
    }
}
