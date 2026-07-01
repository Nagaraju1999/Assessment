package com.nagaraju.stocktracker.feature.stockdetails.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nagaraju.stocktracker.core.common.extensions.toFormattedPrice
import com.nagaraju.stocktracker.domain.model.Stock

@Composable
fun StockStatsRow(
    stock: Stock,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StatColumn(label = "Open", value = stock.openPrice.toFormattedPrice())
        StatColumn(label = "High", value = stock.highPrice.toFormattedPrice())
        StatColumn(label = "Low", value = stock.lowPrice.toFormattedPrice())
        StatColumn(label = "Prev Close", value = stock.previousClose.toFormattedPrice())
    }
}

@Composable
private fun StatColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
