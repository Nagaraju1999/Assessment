package com.nagaraju.stocktracker.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.nagaraju.stocktracker.feature.alerts.navigation.AlertsRoutes
import com.nagaraju.stocktracker.feature.alerts.navigation.alertsRoute
import com.nagaraju.stocktracker.feature.stockdetails.navigation.StockDetailsRoutes
import com.nagaraju.stocktracker.feature.stockdetails.navigation.stockDetailsRoute
import com.nagaraju.stocktracker.feature.watchlist.navigation.WatchlistRoutes
import com.nagaraju.stocktracker.feature.watchlist.navigation.watchlistRoute

/**
 * Top-level navigation graph, assembled from each feature module's own
 * [androidx.navigation.NavGraphBuilder] extension. This is the single file
 * in [app] that knows about all three feature modules — no feature module
 * depends on another, only [app] depends on all of them, which is what
 * allows this aggregation to compile.
 *
 * The watchlist is the start destination since it's the screen a returning
 * user most likely wants to land on; stock details and alerts are both
 * reached from it.
 *
 * Enter/exit transitions are set once here at the [NavHost] level — a
 * standard slide-and-fade — rather than per-destination, so every screen
 * in the app gets consistent transition behavior for free.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = WatchlistRoutes.WATCHLIST,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn()
        },
        exitTransition = {
            fadeOut()
        },
        popEnterTransition = {
            fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut()
        },
    ) {
        watchlistRoute(
            onStockClick = { symbol ->
                navController.navigate(StockDetailsRoutes.createRoute(symbol))
            },
            onNavigateToAlerts = {
                navController.navigate(AlertsRoutes.ALERTS)
            },
        )

        stockDetailsRoute(
            onNavigateBack = { navController.popBackStack() },
        )

        alertsRoute(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
