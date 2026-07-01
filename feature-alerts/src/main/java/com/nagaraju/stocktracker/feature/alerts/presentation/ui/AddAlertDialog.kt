package com.nagaraju.stocktracker.feature.alerts.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nagaraju.stocktracker.domain.model.AlertCondition

/**
 * Dialog form for creating a new price alert.
 *
 * Validation (blank symbol, non-positive price) lives in [AlertsUiState.isAddDialogValid]
 * rather than here, so the same rule the ViewModel/use case applies is what
 * disables the confirm button — no duplicated validation logic between the
 * composable and the business layer.
 */
@Composable
fun AddAlertDialog(
    symbol: String,
    condition: AlertCondition,
    targetPrice: String,
    isValid: Boolean,
    isSubmitting: Boolean,
    onSymbolChange: (String) -> Unit,
    onConditionChange: (AlertCondition) -> Unit,
    onTargetPriceChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New price alert") },
        text = {
            Column {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { onSymbolChange(it.uppercase()) },
                    label = { Text("Symbol") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                ConditionRadioRow(
                    selected = condition,
                    onSelectedChange = onConditionChange,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = onTargetPriceChange,
                    label = { Text("Target price") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("$") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            } else {
                TextButton(onClick = onConfirm, enabled = isValid) {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ConditionRadioRow(
    selected: AlertCondition,
    onSelectedChange: (AlertCondition) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AlertCondition.entries.forEach { condition ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = condition == selected,
                        onClick = { onSelectedChange(condition) },
                    )
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = condition == selected, onClick = { onSelectedChange(condition) })
                Text(if (condition == AlertCondition.ABOVE) "Above" else "Below")
            }
        }
    }
}
