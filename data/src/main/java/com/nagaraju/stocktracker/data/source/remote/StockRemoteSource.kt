package com.nagaraju.stocktracker.data.source.remote

import com.nagaraju.stocktracker.core.network.api.FinnhubApi
import com.nagaraju.stocktracker.core.network.interceptor.RateLimitException
import com.nagaraju.stocktracker.core.network.interceptor.ServerException
import com.nagaraju.stocktracker.core.network.interceptor.UnauthorizedException
import com.nagaraju.stocktracker.core.network.mapper.NetworkCandle
import com.nagaraju.stocktracker.core.network.mapper.NetworkCompanyProfile
import com.nagaraju.stocktracker.core.network.mapper.NetworkQuote
import com.nagaraju.stocktracker.core.network.mapper.NetworkSearchItem
import com.nagaraju.stocktracker.core.network.mapper.toNetworkCandleOrNull
import com.nagaraju.stocktracker.core.network.mapper.toNetworkCompanyProfile
import com.nagaraju.stocktracker.core.network.mapper.toNetworkQuote
import com.nagaraju.stocktracker.core.network.mapper.toNetworkSearchItem
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.TimeRange
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Wraps every [FinnhubApi] call, converting DTOs to network models and
 * translating network-layer exceptions into [DomainException] subtypes.
 *
 * Includes automatic retry with exponential backoff for rate-limit (429)
 * responses — on Finnhub's free tier a burst of simultaneous requests from
 * background polling + a user action can briefly exceed the 60/minute ceiling.
 * Retrying silently after a short wait recovers these cases without surfacing
 * an error to the user.
 */
class StockRemoteSource @Inject constructor(
    private val api: FinnhubApi,
) {
    suspend fun getQuote(symbol: String): NetworkQuote =
        withRetry { api.getQuote(symbol).toNetworkQuote(symbol) }

    suspend fun getCandles(symbol: String, range: TimeRange): NetworkCandle? =
        withRetry {
            val now = System.currentTimeMillis() / 1_000L
            val from = now - range.rangeSeconds
            api.getCandles(symbol, range.resolution, from, now).toNetworkCandleOrNull()
        }

    suspend fun searchSymbols(query: String): List<NetworkSearchItem> =
        withRetry { api.searchSymbol(query).results.map { it.toNetworkSearchItem() } }

    suspend fun getCompanyProfile(symbol: String): NetworkCompanyProfile =
        withRetry { api.getCompanyProfile(symbol).toNetworkCompanyProfile() }

    /**
     * Executes [block], retrying up to [maxRetries] times on a
     * [RateLimitException] (HTTP 429) with exponential backoff.
     * Any other exception is translated immediately to a [DomainException].
     *
     * Backoff schedule: 2s → 4s → give up and surface the error.
     * This covers the common case of a brief burst exceeding Finnhub's
     * free-tier 60/minute window — the window resets within seconds and
     * the retry succeeds without the user ever seeing an error.
     */
    private suspend fun <T> withRetry(
        maxRetries: Int = 2,
        initialDelayMillis: Long = 2_000L,
        block: suspend () -> T,
    ): T {
        var delayMillis = initialDelayMillis
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: RateLimitException) {
                delay(delayMillis)
                delayMillis *= 2
            } catch (e: Throwable) {
                throw e.toDomainException()
            }
        }
        // Final attempt — let any exception propagate as a DomainException.
        return runCatching { block() }.getOrElse { throw it.toDomainException() }
    }

    private fun Throwable.toDomainException(symbol: String? = null): DomainException = when (this) {
        is UnauthorizedException         -> DomainException.UnauthorizedException(this)
        is RateLimitException            -> DomainException.RateLimitExceededException(this)
        is ServerException               -> DomainException.ServerException(this)
        is SocketTimeoutException        -> DomainException.TimeoutException(this)
        is java.net.UnknownHostException -> DomainException.NoInternetException(this)
        is IOException                   -> DomainException.NoInternetException(this)
        is DomainException               -> this
        else                             -> DomainException.UnknownException(this)
    }
}
