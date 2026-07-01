package com.nagaraju.stocktracker.feature.alerts.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nagaraju.stocktracker.core.common.extensions.toFormattedPrice
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert

@Composable
fun AlertListItem(
    alert: StockAlert,
    onEnabledToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val conditionText = when (alert.condition) {
        AlertCondition.ABOVE -> "Above"
        AlertCondition.BELOW -> "Below"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alert.symbol, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$conditionText ${alert.targetPrice.toFormattedPrice()}" +
                        if (alert.isTriggered) " · Triggered" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = alert.isEnabled,
                onCheckedChange = onEnabledToggle,
                modifier = Modifier.semantics {
                    contentDescription = "Toggle alert for ${alert.symbol}"
                },
            )

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete alert",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
