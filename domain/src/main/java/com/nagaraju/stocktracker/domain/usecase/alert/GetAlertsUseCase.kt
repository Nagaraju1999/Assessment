package com.nagaraju.stocktracker.domain.usecase.alert

import com.nagaraju.stocktracker.domain.model.StockAlert
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlertsUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) {
    operator fun invoke(): Flow<List<StockAlert>> = alertRepository.observeAlerts()
}
