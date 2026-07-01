package com.nagaraju.stocktracker.domain.usecase.stock

import com.nagaraju.stocktracker.domain.DEFAULT_POLL_INTERVAL_MS
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockDetailsUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    operator fun invoke(symbol: String, pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MS): Flow<Result<Stock>> =
        stockRepository.observeStock(symbol, pollIntervalMillis)
}
