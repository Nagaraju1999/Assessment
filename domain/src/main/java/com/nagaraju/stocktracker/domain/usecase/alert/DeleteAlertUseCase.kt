package com.nagaraju.stocktracker.domain.usecase.alert

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import javax.inject.Inject

class DeleteAlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository,
) {
    suspend operator fun invoke(alertId: Long): Result<Unit> =
        alertRepository.deleteAlert(alertId)
}
