package com.nagaraju.stocktracker.feature.stockdetails.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nagaraju.stocktracker.core.common.extensions.toFormattedChange
import com.nagaraju.stocktracker.core.common.extensions.toFormattedPrice
import com.nagaraju.stocktracker.core.ui.components.ErrorState
import com.nagaraju.stocktracker.core.ui.components.LoadingIndicator
import com.nagaraju.stocktracker.core.ui.components.PriceChangeChip
import com.nagaraju.stocktracker.core.ui.components.StockTrackerTopBar
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.feature.stockdetails.presentation.state.StockDetailsEvent
import com.nagaraju.stocktracker.feature.stockdetails.presentation.state.StockDetailsUiState
import com.nagaraju.stocktracker.feature.stockdetails.presentation.viewmodel.StockDetailsViewModel

/**
 * Stateful entry point wired into the nav graph. Mirrors the watchlist
 * feature's Route/Screen split: collect state and events here, render via
 * the stateless [StockDetailsScreen].
 */
@Composable
fun StockDetailsRoute(
    onNavigateBack: () -> Unit,
    viewModel: StockDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StockDetailsEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is StockDetailsEvent.AddedToWatchlist ->
                    snackbarHostState.showSnackbar("${event.symbol} added to watchlist")
                is StockDetailsEvent.RemovedFromWatchlist ->
                    snackbarHostState.showSnackbar("${event.symbol} removed from watchlist")
            }
        }
    }

    StockDetailsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onRangeSelected = viewModel::onRangeSelected,
        onWatchlistToggleClick = viewModel::onWatchlistToggleClick,
        onRetryHistoryClick = viewModel::onRetryHistoryClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsScreen(
    uiState: StockDetailsUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onRangeSelected: (TimeRange) -> Unit,
    onWatchlistToggleClick: () -> Unit,
    onRetryHistoryClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            StockTrackerTopBar(
                title = uiState.symbol,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onWatchlistToggleClick) {
                        Icon(
                            imageVector = if (uiState.isInWatchlist) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = if (uiState.isInWatchlist) {
                                "Remove from watchlist"
                            } else {
                                "Add to watchlist"
                            },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isQuoteLoading -> LoadingIndicator(
                modifier = Modifier.padding(paddingValues),
            )

            uiState.quoteErrorMessage != null -> ErrorState(
                message = uiState.quoteErrorMessage,
                modifier = Modifier.padding(paddingValues),
            )

            else -> StockDetailsContent(
                uiState = uiState,
                onRangeSelected = onRangeSelected,
                onRetryHistoryClick = onRetryHistoryClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun StockDetailsContent(
    uiState: StockDetailsUiState,
    onRangeSelected: (TimeRange) -> Unit,
    onRetryHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stock = uiState.stock ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!uiState.companyProfile?.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = uiState.companyProfile?.logoUrl,
                    contentDescription = null, // decorative — company name text label conveys identity
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(text = stock.companyName, style = MaterialTheme.typography.titleMedium)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stock.currentPrice.toFormattedPrice(), style = MaterialTheme.typography.headlineLarge)
            PriceChangeChip(
                text = "${stock.change.toFormattedPrice()} (${stock.percentChange.toFormattedChange()})",
                isPositive = stock.isPositive,
            )
        }

        when {
            uiState.isHistoryLoading -> LoadingIndicator(modifier = Modifier.padding(vertical = 32.dp))

            uiState.historyErrorMessage != null -> ErrorState(
                message = uiState.historyErrorMessage,
                onRetry = onRetryHistoryClick,
            )

            uiState.history != null -> PriceChart(candles = uiState.history.candles)
        }

        uiState.priceTrend?.let { trend ->
            PriceTrendIndicator(trend = trend)
        }

        TimeRangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = onRangeSelected,
        )

        HorizontalDivider()

        StockStatsRow(stock = stock)
    }
}
