package com.nagaraju.stocktracker.domain.usecase.watchlist

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.repository.StockRepository
import javax.inject.Inject

class RemoveFromWatchlistUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    suspend operator fun invoke(symbol: String): Result<Unit> =
        stockRepository.removeFromWatchlist(symbol)
}
