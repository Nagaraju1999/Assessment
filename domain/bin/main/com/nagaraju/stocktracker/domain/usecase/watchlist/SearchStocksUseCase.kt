package com.nagaraju.stocktracker.domain.usecase.watchlist

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.StockSearchResult
import com.nagaraju.stocktracker.domain.repository.StockRepository
import javax.inject.Inject

private const val MIN_QUERY_LENGTH = 1

/**
 * Searches for stock symbols matching a free-text query.
 *
 * Short-circuits with an empty success result for blank queries instead of
 * hitting the network — avoids wasting an API call (and rate-limit budget)
 * on a query the user hasn't finished typing.
 */
class SearchStocksUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    suspend operator fun invoke(query: String): Result<List<StockSearchResult>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < MIN_QUERY_LENGTH) {
            return Result.Success(emptyList())
        }
        return stockRepository.searchStocks(trimmedQuery)
    }
}
