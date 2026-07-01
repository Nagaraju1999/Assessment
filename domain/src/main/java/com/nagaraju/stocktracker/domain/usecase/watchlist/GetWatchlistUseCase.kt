package com.nagaraju.stocktracker.domain.usecase.watchlist

import com.nagaraju.stocktracker.domain.DEFAULT_POLL_INTERVAL_MS
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the user's watchlist with live, continuously polling quote data.
 *
 * A thin pass-through over [StockRepository.observeWatchlist] — kept as its
 * own use case (rather than calling the repository directly from the
 * ViewModel) so the polling interval is a single point of business policy,
 * not duplicated across whichever screen happens to need it.
 */
class GetWatchlistUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    operator fun invoke(pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MS): Flow<Result<List<Stock>>> =
        stockRepository.observeWatchlist(pollIntervalMillis)
}
