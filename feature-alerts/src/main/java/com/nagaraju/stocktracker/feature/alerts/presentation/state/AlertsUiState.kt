package com.nagaraju.stocktracker.feature.alerts.presentation.state

import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert

data class AlertsUiState(
    val alerts: List<StockAlert> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isAddDialogVisible: Boolean = false,
    val addDialogSymbol: String = "",
    val addDialogCondition: AlertCondition = AlertCondition.ABOVE,
    val addDialogTargetPrice: String = "",
    val isSubmittingAlert: Boolean = false,
) {
    val isEmpty: Boolean get() = alerts.isEmpty() && !isLoading && errorMessage == null

    /**
     * Whether the add-alert form has enough valid input to submit.
     * A blank symbol or a target price that doesn't parse to a positive
     * number both keep the confirm button disabled — this mirrors
     * [com.nagaraju.stocktracker.domain.usecase.alert.CreateAlertUseCase]'s
     * own validation so the user gets instant feedback instead of waiting
     * for a round trip to discover the same rule.
     */
    val isAddDialogValid: Boolean get() =
        addDialogSymbol.isNotBlank() && (addDialogTargetPrice.toDoubleOrNull() ?: 0.0) > 0.0
}
