package com.nagaraju.stocktracker.feature.watchlist.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nagaraju.stocktracker.core.common.extensions.toFormattedChange
import com.nagaraju.stocktracker.core.common.extensions.toFormattedPrice
import com.nagaraju.stocktracker.core.common.formatter.DateFormatter
import com.nagaraju.stocktracker.core.ui.components.PriceChangeChip
import com.nagaraju.stocktracker.domain.model.Stock

/**
 * A single row in the watchlist list. Tapping navigates to stock details;
 * long-pressing surfaces the remove action via [onLongClick] — chosen over
 * a persistent delete icon to keep the row visually focused on price data,
 * with destructive actions requiring deliberate intent.
 *
 * The long-press gesture has no visible affordance and is not discoverable
 * by screen reader users, so [onLongClick] is additionally exposed as a
 * custom accessibility action ("Remove from watchlist"), reachable through
 * TalkBack's local context menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StockListItem(
    stock: Stock,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction("Remove ${stock.symbol} from watchlist") {
                        onLongClick()
                        true
                    },
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stock.companyName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (stock.isCached) {
                    Text(
                        text = "Offline · last updated ${DateFormatter.toTime(stock.priceTimestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stock.currentPrice.toFormattedPrice(),
                    style = MaterialTheme.typography.titleMedium,
                )
                PriceChangeChip(
                    text = stock.percentChange.toFormattedChange(),
                    isPositive = stock.isPositive,
                )
            }
        }
    }
}
