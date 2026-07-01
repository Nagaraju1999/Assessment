package com.nagaraju.stocktracker.feature.stockdetails.presentation.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nagaraju.stocktracker.core.common.extensions.toFormattedPrice
import com.nagaraju.stocktracker.domain.model.PriceTrend
import com.nagaraju.stocktracker.domain.model.TrendDirection

/**
 * Displays [PriceTrend] as a small card: a directional arrow, a projected
 * next-bar price, and a confidence percentage.
 *
 * This is presented as a directional signal derived from recent price
 * history, not a guarantee — the confidence percentage is shown precisely
 * so the indicator doesn't overstate its own reliability for a noisy series.
 */
@Composable
fun PriceTrendIndicator(
    trend: PriceTrend,
    modifier: Modifier = Modifier,
) {
    val (icon, label, color) = when (trend.direction) {
        TrendDirection.UP -> Triple(Icons.Filled.TrendingUp, "Trending up", MaterialTheme.colorScheme.secondary)
        TrendDirection.DOWN -> Triple(Icons.Filled.TrendingDown, "Trending down", MaterialTheme.colorScheme.error)
        TrendDirection.FLAT -> Triple(Icons.Filled.TrendingFlat, "Flat", MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label · next ${trend.projectedNextClose.toFormattedPrice()} " +
                    "(${(trend.confidence * 100).toInt()}% confidence)",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
