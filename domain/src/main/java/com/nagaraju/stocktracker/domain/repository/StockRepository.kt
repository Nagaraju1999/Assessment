package com.nagaraju.stocktracker.domain.repository

import com.nagaraju.stocktracker.domain.DEFAULT_POLL_INTERVAL_MS
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.CompanyProfile
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.StockSearchResult
import com.nagaraju.stocktracker.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth contract for stock data, implemented by the
 * [data] module. The domain layer depends only on this interface — never
 * on Retrofit or Room directly.
 */
interface StockRepository {

    /**
     * Observes the user's watchlist with live quote data, polling the
     * network every [pollIntervalMillis] while collected.
     *
     * Emits [Result.Loading] first, then [Result.Success] on each successful
     * poll, or [Result.Error] if a poll fails (the previous successful data
     * remains visible in the UI — the ViewModel decides how to merge this).
     */
    fun observeWatchlist(pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MS): Flow<Result<List<Stock>>>

    /** Adds [symbol] to the watchlist, fetching its company name for display. */
    suspend fun addToWatchlist(symbol: String, companyName: String): Result<Unit>

    /** Removes [symbol] from the watchlist. */
    suspend fun removeFromWatchlist(symbol: String): Result<Unit>

    /** Returns `true` if [symbol] is already on the watchlist. */
    fun isInWatchlist(symbol: String): Flow<Boolean>

    /**
     * Observes a single stock's live quote, polling every [pollIntervalMillis]
     * while collected. Used on the stock details screen.
     */
    fun observeStock(symbol: String, pollIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MS): Flow<Result<Stock>>

    /** Fetches historical candle data for [symbol] over the given [range]. */
    suspend fun getStockHistory(symbol: String, range: TimeRange): Result<StockHistory>

    /** Fetches company profile metadata for [symbol]. */
    suspend fun getCompanyProfile(symbol: String): Result<CompanyProfile>

    /** Searches for symbols matching [query]. */
    suspend fun searchStocks(query: String): Result<List<StockSearchResult>>
}
