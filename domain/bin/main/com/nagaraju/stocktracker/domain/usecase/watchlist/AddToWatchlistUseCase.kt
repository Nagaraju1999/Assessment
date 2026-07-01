package com.nagaraju.stocktracker.domain.usecase.watchlist

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.repository.StockRepository
import javax.inject.Inject

/**
 * Adds a stock to the user's watchlist.
 *
 * Validates that [symbol] is non-blank before touching the repository —
 * this is genuine business logic (an empty symbol is never valid), not
 * a pass-through, which justifies this use case's existence as distinct
 * from calling the repository directly.
 */
class AddToWatchlistUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    suspend operator fun invoke(symbol: String, companyName: String): Result<Unit> {
        val trimmedSymbol = symbol.trim().uppercase()
        if (trimmedSymbol.isBlank()) {
            return Result.Error(IllegalArgumentException("Stock symbol cannot be blank"))
        }
        return stockRepository.addToWatchlist(trimmedSymbol, companyName.trim())
    }
}
