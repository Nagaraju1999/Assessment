package com.nagaraju.stocktracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A small color-coded pill showing a price change, e.g. "+1.45%" in green
 * or "-2.30%" in red. Used on both the watchlist row and the stock details
 * header, so it lives in [core-ui] rather than being duplicated per feature.
 *
 * @param isPositive Determines the chip's color — green for gains, red for losses.
 */
@Composable
fun PriceChangeChip(
    text: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isPositive) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    }
    val textColor = if (isPositive) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }

    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
