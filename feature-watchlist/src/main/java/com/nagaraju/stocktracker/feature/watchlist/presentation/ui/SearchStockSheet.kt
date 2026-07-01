package com.nagaraju.stocktracker.feature.watchlist.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nagaraju.stocktracker.core.ui.components.EmptyState
import com.nagaraju.stocktracker.domain.model.StockSearchResult

/**
 * Modal bottom sheet for searching Finnhub symbols and adding one to the
 * watchlist. A bottom sheet (rather than a full new screen) keeps the user
 * anchored to the watchlist context they're adding to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchStockSheet(
    query: String,
    results: List<StockSearchResult>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onResultClick: (symbol: String, companyName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search symbol or company name") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isSearching -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(32.dp)
                        .align(Alignment.CenterHorizontally),
                )

                query.isNotBlank() && results.isEmpty() -> EmptyState(
                    icon = Icons.Filled.Search,
                    title = "No results",
                    message = "No stocks found for \"$query\"",
                    modifier = Modifier.height(200.dp),
                )

                else -> LazyColumn {
                    items(results, key = { it.symbol }) { result ->
                        ListItem(
                            headlineContent = { Text(result.displaySymbol) },
                            supportingContent = { Text(result.description) },
                            modifier = Modifier.clickable {
                                onResultClick(result.symbol, result.description)
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
