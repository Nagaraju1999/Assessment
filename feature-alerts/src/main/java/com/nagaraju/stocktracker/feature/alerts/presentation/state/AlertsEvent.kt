package com.nagaraju.stocktracker.feature.alerts.presentation.state

import com.nagaraju.stocktracker.domain.model.StockAlert

sealed interface AlertsEvent {
    data class ShowError(val message: String) : AlertsEvent
    data class AlertCreated(val symbol: String) : AlertsEvent
    data class AlertDeleted(val symbol: String) : AlertsEvent

    /**
     * A configured alert's price condition has just been satisfied.
     * The screen reacts by showing an in-app Snackbar; [AlertsViewModel]
     * also forwards this to the system notification poster so the user
     * is informed even when the app isn't in the foreground.
     */
    data class AlertTriggered(val alert: StockAlert) : AlertsEvent
}
