package com.nagaraju.stocktracker.feature.watchlist.presentation.ui

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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.nagaraju.stocktracker.feature.watchlist.presentation.state.WatchlistEvent
import com.nagaraju.stocktracker.feature.watchlist.presentation.state.WatchlistUiState
import com.nagaraju.stocktracker.feature.watchlist.presentation.viewmodel.WatchlistViewModel

/**
 * Stateful entry point wired into the nav graph. Collects ViewModel state
 * and one-time events, delegating actual rendering to the stateless
 * [WatchlistScreen] — this split keeps [WatchlistScreen] trivially
 * previewable and testable without a Hilt ViewModel.
 */
@Composable
fun WatchlistRoute(
    onStockClick: (symbol: String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WatchlistEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is WatchlistEvent.StockAdded -> snackbarHostState.showSnackbar("${event.symbol} added to watchlist")
                is WatchlistEvent.StockRemoved -> snackbarHostState.showSnackbar("${event.symbol} removed")
            }
        }
    }

    WatchlistScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onStockClick = onStockClick,
        onStockLongClick = viewModel::onRemoveStockClick,
        onAddStockClick = viewModel::onAddStockClick,
        onNavigateToAlerts = onNavigateToAlerts,
        onSearchSheetDismiss = viewModel::onSearchSheetDismiss,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchResultClick = viewModel::onSearchResultClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    uiState: WatchlistUiState,
    snackbarHostState: SnackbarHostState,
    onStockClick: (symbol: String) -> Unit,
    onStockLongClick: (symbol: String) -> Unit,
    onAddStockClick: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onSearchSheetDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchResultClick: (symbol: String, companyName: String) -> Unit,
) {
    Scaffold(
        topBar = {
            StockTrackerTopBar(
                title = "Watchlist",
                actions = {
                    IconButton(onClick = onNavigateToAlerts) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Alerts")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStockClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add stock")
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
                uiState.isLoading -> WatchlistContentState.LOADING
                uiState.errorMessage != null -> WatchlistContentState.ERROR
                uiState.isEmpty -> WatchlistContentState.EMPTY
                else -> WatchlistContentState.CONTENT
            }

            Crossfade(targetState = contentState, label = "watchlist_content_crossfade") { state ->
                when (state) {
                    WatchlistContentState.LOADING -> LoadingIndicator()

                    WatchlistContentState.ERROR -> ErrorState(message = uiState.errorMessage.orEmpty())

                    WatchlistContentState.EMPTY -> EmptyState(
                        icon = Icons.Filled.ShowChart,
                        title = "Your watchlist is empty",
                        message = "Tap the + button to add your first stock",
                    )

                    WatchlistContentState.CONTENT -> WatchlistContent(
                        uiState = uiState,
                        onStockClick = onStockClick,
                        onStockLongClick = onStockLongClick,
                    )
                }
            }
        }

        if (uiState.isSearchSheetVisible) {
            SearchStockSheet(
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isSearching = uiState.isSearching,
                onQueryChange = onSearchQueryChange,
                onResultClick = onSearchResultClick,
                onDismiss = onSearchSheetDismiss,
            )
        }
    }
}

/** Discriminates which top-level content [Crossfade] should animate between. */
private enum class WatchlistContentState { LOADING, ERROR, EMPTY, CONTENT }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WatchlistContent(
    uiState: WatchlistUiState,
    onStockClick: (symbol: String) -> Unit,
    onStockLongClick: (symbol: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(uiState.stocks, key = { it.symbol }) { stock ->
            StockListItem(
                stock = stock,
                onClick = { onStockClick(stock.symbol) },
                onLongClick = { onStockLongClick(stock.symbol) },
                modifier = Modifier.animateItemPlacement(),
            )
        }
    }
}
