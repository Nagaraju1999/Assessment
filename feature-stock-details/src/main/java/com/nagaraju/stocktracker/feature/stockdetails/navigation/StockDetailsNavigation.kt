package com.nagaraju.stocktracker.feature.stockdetails.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nagaraju.stocktracker.feature.stockdetails.presentation.ui.StockDetailsRoute

/**
 * Registers the stock details destination, including its required [symbol]
 * path argument and the `stocktracker://stock/{symbol}` deep link that
 * lets a triggered-alert notification open directly to this screen.
 */
fun NavGraphBuilder.stockDetailsRoute(
    onNavigateBack: () -> Unit,
) {
    composable(
        route = StockDetailsRoutes.STOCK_DETAILS_PATTERN,
        arguments = listOf(navArgument(StockDetailsRoutes.SYMBOL_ARG) { type = NavType.StringType }),
        deepLinks = StockDetailsRoutes.deepLinks(),
    ) {
        StockDetailsRoute(onNavigateBack = onNavigateBack)
    }
}
