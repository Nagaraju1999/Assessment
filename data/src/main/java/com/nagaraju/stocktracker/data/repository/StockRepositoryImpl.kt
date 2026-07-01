package com.nagaraju.stocktracker.data.repository

import com.nagaraju.stocktracker.core.common.dispatcher.DispatcherProvider
import com.nagaraju.stocktracker.core.common.extensions.pollingFlow
import com.nagaraju.stocktracker.core.common.network.NetworkMonitor
import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity
import com.nagaraju.stocktracker.data.mapper.toDomain
import com.nagaraju.stocktracker.data.source.local.StockLocalSource
import com.nagaraju.stocktracker.data.source.remote.StockRemoteSource
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.CompanyProfile
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.StockSearchResult
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.domain.repository.StockRepository
import com.nagaraju.stocktracker.domain.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val remoteSource: StockRemoteSource,
    private val localSource: StockLocalSource,
    private val networkMonitor: NetworkMonitor,
    private val dispatcherProvider: DispatcherProvider,
) : StockRepository {

    override fun observeWatchlist(pollIntervalMillis: Long): Flow<Result<List<Stock>>> =
        combine(
            localSource.observeWatchlist(),
            pollingFlow(pollIntervalMillis) { Unit }, // ticks every interval to trigger a re-fetch
        ) { entities, _ -> entities }
            .map { entities -> fetchQuotesFor(entities) }
            .map<List<Stock>, Result<List<Stock>>> { Result.Success(it) }
            .catch { emit(Result.Error(it)) }
            .flowOn(dispatcherProvider.io)

    /**
     * Fetches a live quote for every watchlist entity. A single symbol's
     * quote failing does not fail the whole list.
     *
     * Offline/sync strategy:
     *  - On a successful fetch, the quote is mapped to [Stock] *and*
     *    persisted into the entity's cache columns via [StockLocalSource.updateCachedQuote]
     *    (write-through — every live read refreshes the offline fallback).
     *  - On a failed fetch, the entity's last cached quote (if any) is
     *    returned instead, marked [Stock.isCached] = true so the UI can
     *    show a "last updated at HH:mm" indicator rather than presenting
     *    stale numbers as if they were live.
     *  - If there is no cache yet (first-ever fetch failed), the row still
     *    renders with identity-only zeroed price fields rather than being
     *    dropped from the list entirely.
     */
    private suspend fun fetchQuotesFor(entities: List<WatchlistEntity>): List<Stock> =
        entities.map { entity ->
            runCatching {
                val quote = remoteSource.getQuote(entity.symbol)
                localSource.updateCachedQuote(
                    symbol        = entity.symbol,
                    price         = quote.currentPrice,
                    change        = quote.change,
                    percentChange = quote.percentChange,
                    cachedAt      = System.currentTimeMillis() / 1_000L,
                )
                entity.toDomain(quote)
            }.getOrElse {
                entity.toCachedDomainOrZeroed()
            }
        }

    /**
     * Builds a [Stock] from this entity's cached price columns when present,
     * or a zeroed-out [Stock] (identity only) when no successful poll has
     * ever populated the cache for this symbol.
     */
    private fun WatchlistEntity.toCachedDomainOrZeroed(): Stock {
        val price = cachedPrice
        val cachedTimestamp = cachedAt
        return if (price != null && cachedTimestamp != null) {
            Stock(
                symbol         = symbol,
                companyName    = companyName,
                currentPrice   = price,
                change         = cachedChange ?: 0.0,
                percentChange  = cachedPercentChange ?: 0.0,
                highPrice      = price,
                lowPrice       = price,
                openPrice      = price,
                previousClose  = price - (cachedChange ?: 0.0),
                addedAt        = addedAt,
                isCached       = true,
                priceTimestamp = cachedTimestamp,
            )
        } else {
            Stock(
                symbol        = symbol,
                companyName   = companyName,
                currentPrice  = 0.0,
                change        = 0.0,
                percentChange = 0.0,
                highPrice     = 0.0,
                lowPrice      = 0.0,
                openPrice     = 0.0,
                previousClose = 0.0,
                addedAt       = addedAt,
            )
        }
    }

    override suspend fun addToWatchlist(symbol: String, companyName: String): Result<Unit> =
        runCatching {
            if (!networkMonitor.isConnected.first()) {
                throw DomainException.NoInternetException()
            }
            // Symbol is already validated by the search result that produced it —
            // no extra getQuote() needed here, which saves a network call and
            // avoids a rate-limit hit when background polling is also running.
            localSource.addToWatchlist(
                WatchlistEntity(
                    symbol      = symbol,
                    companyName = companyName,
                    addedAt     = System.currentTimeMillis() / 1_000L,
                ),
            )
            Result.Success(Unit)
        }.getOrElse { Result.Error(it) }

    override suspend fun removeFromWatchlist(symbol: String): Result<Unit> =
        runCatching {
            localSource.removeFromWatchlist(symbol)
            Result.Success(Unit)
        }.getOrElse { Result.Error(it) }

    override fun isInWatchlist(symbol: String): Flow<Boolean> =
        localSource.observeIsInWatchlist(symbol)

    override fun observeStock(symbol: String, pollIntervalMillis: Long): Flow<Result<Stock>> =
        pollingFlow(pollIntervalMillis) {
            val entity = localSource.observeWatchlist().first().find { it.symbol == symbol }
            val companyName = entity?.companyName ?: symbol
            val quote = remoteSource.getQuote(symbol)
            Stock(
                symbol        = symbol,
                companyName   = companyName,
                currentPrice  = quote.currentPrice,
                change        = quote.change,
                percentChange = quote.percentChange,
                highPrice     = quote.highPrice,
                lowPrice      = quote.lowPrice,
                openPrice     = quote.openPrice,
                previousClose = quote.previousClose,
                addedAt       = entity?.addedAt ?: 0L,
            )
        }
            .map<Stock, Result<Stock>> { Result.Success(it) }
            .catch { emit(Result.Error(it)) }
            .flowOn(dispatcherProvider.io)

    override suspend fun getStockHistory(symbol: String, range: TimeRange): Result<StockHistory> =
        runCatching {
            val candle = remoteSource.getCandles(symbol, range)
                ?: return Result.Error(DomainException.EmptyResponseException(symbol))
            Result.Success(candle.toDomain(symbol, range))
        }.getOrElse { Result.Error(it) }

    override suspend fun getCompanyProfile(symbol: String): Result<CompanyProfile> =
        runCatching {
            Result.Success(remoteSource.getCompanyProfile(symbol).toDomain(symbol))
        }.getOrElse { Result.Error(it) }

    override suspend fun searchStocks(query: String): Result<List<StockSearchResult>> =
        runCatching {
            if (!networkMonitor.isConnected.first()) {
                throw DomainException.NoInternetException()
            }
            Result.Success(remoteSource.searchSymbols(query).map { it.toDomain() })
        }.getOrElse { Result.Error(it) }
}
