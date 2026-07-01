package com.nagaraju.stocktracker.data.source.local

import com.nagaraju.stocktracker.core.database.dao.WatchlistDao
import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Thin wrapper over [WatchlistDao]. Exists as its own class (rather than
 * injecting the DAO directly into the repository) so the repository layer
 * never imports `core-database` types directly — keeping the dependency
 * boundary explicit and the repository implementation easier to test
 * with a single mock instead of two.
 */
class StockLocalSource @Inject constructor(
    private val watchlistDao: WatchlistDao,
) {
    fun observeWatchlist(): Flow<List<WatchlistEntity>> = watchlistDao.observeAll()

    fun observeIsInWatchlist(symbol: String): Flow<Boolean> =
        watchlistDao.observeBySymbol(symbol).map { it != null }

    suspend fun addToWatchlist(entity: WatchlistEntity) = watchlistDao.insert(entity)

    suspend fun removeFromWatchlist(symbol: String) = watchlistDao.deleteBySymbol(symbol)

    suspend fun isInWatchlist(symbol: String): Boolean = watchlistDao.exists(symbol)

    /** Persists the latest successful quote as the offline-fallback cache for [symbol]. */
    suspend fun updateCachedQuote(
        symbol: String,
        price: Double,
        change: Double,
        percentChange: Double,
        cachedAt: Long,
    ) = watchlistDao.updateCachedQuote(symbol, price, change, percentChange, cachedAt)
}
