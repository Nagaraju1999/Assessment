package com.nagaraju.stocktracker.domain.usecase.alert

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import javax.inject.Inject

/**
 * Creates a new price alert.
 *
 * Validates that [targetPrice] is strictly positive before persisting —
 * a zero or negative threshold is never a meaningful alert condition.
 */
class CreateAlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) {
    suspend operator fun invoke(
        symbol: String,
        condition: AlertCondition,
        targetPrice: Double,
    ): Result<Long> {
        if (targetPrice <= 0.0) {
            return Result.Error(IllegalArgumentException("Target price must be greater than zero"))
        }
        return alertRepository.createAlert(symbol.trim().uppercase(), condition, targetPrice)
    }
}
