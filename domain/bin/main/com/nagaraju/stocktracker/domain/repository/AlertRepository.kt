package com.nagaraju.stocktracker.domain.repository

import com.nagaraju.stocktracker.domain.DEFAULT_POLL_INTERVAL_MS
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth contract for alert data, implemented by the
 * [data] module.
 */
interface AlertRepository {

    /** Observes every configured alert, most recently created first. */
    fun observeAlerts(): Flow<List<StockAlert>>

    /** Creates a new alert and returns its assigned id. */
    suspend fun createAlert(
        symbol: String,
        condition: AlertCondition,
        targetPrice: Double,
    ): Result<Long>

    /** Deletes the alert identified by [alertId]. */
    suspend fun deleteAlert(alertId: Long): Result<Unit>

    /** Enables or disables the alert identified by [alertId]. */
    suspend fun setAlertEnabled(alertId: Long, isEnabled: Boolean): Result<Unit>

    /**
     * Continuously evaluates active alerts against live prices, polling
     * every [pollIntervalMillis]. Emits a [StockAlert] each time one
     * transitions into its triggered state — the ViewModel surfaces this
     * as a one-time notification event.
     *
     * Triggered alerts are marked as such in the database so they are
     * not re-emitted on subsequent polls.
     */
    fun observeTriggeredAlerts(pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MS): Flow<StockAlert>
}
