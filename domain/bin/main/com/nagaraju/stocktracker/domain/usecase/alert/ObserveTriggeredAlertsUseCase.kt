package com.nagaraju.stocktracker.domain.usecase.alert

import com.nagaraju.stocktracker.domain.DEFAULT_POLL_INTERVAL_MS
import com.nagaraju.stocktracker.domain.model.StockAlert
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes alerts transitioning into their triggered state, so the
 * presentation layer can surface a notification (in-app and/or system)
 * the moment a price condition is met.
 */
class ObserveTriggeredAlertsUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) {
    operator fun invoke(pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MS): Flow<StockAlert> =
        alertRepository.observeTriggeredAlerts(pollIntervalMillis)
}
