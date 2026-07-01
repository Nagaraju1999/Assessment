package com.nagaraju.stocktracker.feature.alerts.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nagaraju.stocktracker.core.ui.components.EmptyState
import com.nagaraju.stocktracker.core.ui.components.ErrorState
import com.nagaraju.stocktracker.core.ui.components.LoadingIndicator
import com.nagaraju.stocktracker.core.ui.components.StockTrackerTopBar
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.feature.alerts.presentation.state.AlertsEvent
import com.nagaraju.stocktracker.feature.alerts.presentation.state.AlertsUiState
import com.nagaraju.stocktracker.feature.alerts.presentation.viewmodel.AlertsViewModel

/**
 * Stateful entry point wired into the nav graph. Same Route/Screen split
 * used by the other two features.
 */
@Composable
fun AlertsRoute(
    onNavigateBack: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AlertsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is AlertsEvent.AlertCreated -> snackbarHostState.showSnackbar("Alert created for ${event.symbol}")
                is AlertsEvent.AlertDeleted -> snackbarHostState.showSnackbar("Alert deleted")
                is AlertsEvent.AlertTriggered ->
                    snackbarHostState.showSnackbar("${event.alert.symbol} hit your target price!")
            }
        }
    }

    AlertsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onAddAlertClick = viewModel::onAddAlertClick,
        onAddDialogDismiss = viewModel::onAddDialogDismiss,
        onAddDialogSymbolChange = viewModel::onAddDialogSymbolChange,
        onAddDialogConditionChange = viewModel::onAddDialogConditionChange,
        onAddDialogTargetPriceChange = viewModel::onAddDialogTargetPriceChange,
        onAddDialogConfirm = viewModel::onAddDialogConfirm,
        onDeleteAlertClick = viewModel::onDeleteAlertClick,
        onAlertEnabledToggle = viewModel::onAlertEnabledToggle,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlertsScreen(
    uiState: AlertsUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAddAlertClick: () -> Unit,
    onAddDialogDismiss: () -> Unit,
    onAddDialogSymbolChange: (String) -> Unit,
    onAddDialogConditionChange: (AlertCondition) -> Unit,
    onAddDialogTargetPriceChange: (String) -> Unit,
    onAddDialogConfirm: () -> Unit,
    onDeleteAlertClick: (alertId: Long, symbol: String) -> Unit,
    onAlertEnabledToggle: (alertId: Long, isEnabled: Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            StockTrackerTopBar(title = "Alerts", onNavigateBack = onNavigateBack)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlertClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add alert")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val contentState = when {
                uiState.isLoading -> AlertsContentState.LOADING
                uiState.errorMessage != null -> AlertsContentState.ERROR
                uiState.isEmpty -> AlertsContentState.EMPTY
                else -> AlertsContentState.CONTENT
            }

            Crossfade(targetState = contentState, label = "alerts_content_crossfade") { state ->
                when (state) {
                    AlertsContentState.LOADING -> LoadingIndicator()

                    AlertsContentState.ERROR -> ErrorState(message = uiState.errorMessage.orEmpty())

                    AlertsContentState.EMPTY -> EmptyState(
                        icon = Icons.Filled.NotificationsNone,
                        title = "No alerts yet",
                        message = "Tap the + button to create your first price alert",
                    )

                    AlertsContentState.CONTENT -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.alerts, key = { it.id }) { alert ->
                            AlertListItem(
                                alert = alert,
                                onEnabledToggle = { isEnabled -> onAlertEnabledToggle(alert.id, isEnabled) },
                                onDeleteClick = { onDeleteAlertClick(alert.id, alert.symbol) },
                                modifier = Modifier.animateItemPlacement(),
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isAddDialogVisible) {
            AddAlertDialog(
                symbol = uiState.addDialogSymbol,
                condition = uiState.addDialogCondition,
                targetPrice = uiState.addDialogTargetPrice,
                isValid = uiState.isAddDialogValid,
                isSubmitting = uiState.isSubmittingAlert,
                onSymbolChange = onAddDialogSymbolChange,
                onConditionChange = onAddDialogConditionChange,
                onTargetPriceChange = onAddDialogTargetPriceChange,
                onConfirm = onAddDialogConfirm,
                onDismiss = onAddDialogDismiss,
            )
        }
    }
}

/** Discriminates which top-level content [Crossfade] should animate between. */
private enum class AlertsContentState { LOADING, ERROR, EMPTY, CONTENT }
