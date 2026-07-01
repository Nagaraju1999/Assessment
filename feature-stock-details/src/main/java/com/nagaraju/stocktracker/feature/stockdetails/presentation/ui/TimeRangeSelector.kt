package com.nagaraju.stocktracker.feature.stockdetails.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nagaraju.stocktracker.domain.model.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = range == selectedRange,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
            )
        }
    }
}
