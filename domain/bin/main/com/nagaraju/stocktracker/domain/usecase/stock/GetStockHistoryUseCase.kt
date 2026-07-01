package com.nagaraju.stocktracker.domain.usecase.stock

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.domain.repository.StockRepository
import javax.inject.Inject

/**
 * Fetches historical price candles for the chart on the stock details screen.
 *
 * Converts an empty-but-successful candle list into a [DomainException.EmptyResponseException]
 * — genuine business logic, since "the API call succeeded but returned zero
 * data points" should be treated as an error state by the UI, not a valid
 * empty chart.
 */
class GetStockHistoryUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    suspend operator fun invoke(symbol: String, range: TimeRange): Result<StockHistory> {
        val result = stockRepository.getStockHistory(symbol, range)
        if (result is Result.Success && result.data.candles.isEmpty()) {
            return Result.Error(DomainException.EmptyResponseException(symbol))
        }
        return result
    }
}
