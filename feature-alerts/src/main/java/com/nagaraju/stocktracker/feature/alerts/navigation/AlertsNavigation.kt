package com.nagaraju.stocktracker.feature.alerts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nagaraju.stocktracker.feature.alerts.presentation.ui.AlertsRoute

fun NavGraphBuilder.alertsRoute(
    onNavigateBack: () -> Unit,
) {
    composable(route = AlertsRoutes.ALERTS) {
        AlertsRoute(onNavigateBack = onNavigateBack)
    }
}
