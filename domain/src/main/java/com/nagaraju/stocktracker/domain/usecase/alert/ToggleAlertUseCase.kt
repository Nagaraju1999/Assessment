package com.nagaraju.stocktracker.domain.usecase.alert

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import javax.inject.Inject

class ToggleAlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) {
    suspend operator fun invoke(alertId: Long, isEnabled: Boolean): Result<Unit> =
        alertRepository.setAlertEnabled(alertId, isEnabled)
}
