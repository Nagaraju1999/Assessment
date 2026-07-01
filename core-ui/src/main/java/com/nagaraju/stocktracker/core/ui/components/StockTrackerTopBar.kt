package com.nagaraju.stocktracker.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared top app bar used by every feature screen. Centralizing this avoids
 * each screen re-declaring [TopAppBar] with slightly different padding or
 * elevation choices.
 *
 * @param onNavigateBack When non-null, shows a back arrow that invokes this.
 * @param actions         Trailing icon buttons, e.g. a search or alerts icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTrackerTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                    )
                }
            }
        },
        actions = { actions() },
    )
}
