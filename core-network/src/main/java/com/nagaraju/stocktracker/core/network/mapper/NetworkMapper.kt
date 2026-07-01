package com.nagaraju.stocktracker.core.network.mapper

import com.nagaraju.stocktracker.core.network.dto.CandleDto
import com.nagaraju.stocktracker.core.network.dto.CompanyProfileDto
import com.nagaraju.stocktracker.core.network.dto.QuoteDto
import com.nagaraju.stocktracker.core.network.dto.SearchItemDto

/**
 * Intermediate network models — thin wrappers that exist between DTOs and
 * domain models. They carry only fields the app actually uses, with nulls
 * resolved to sensible defaults so the domain layer never sees raw API nulls.
 *
 * The actual domain model mapping happens in the [data] module's mapper classes,
 * which combine network models with database entities.
 */

data class NetworkQuote(
    val symbol: String,
    val currentPrice: Double,
    val change: Double,
    val percentChange: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val openPrice: Double,
    val previousClose: Double,
    val timestamp: Long,
)

data class NetworkCandle(
    val timestamps: List<Long>,
    val closePrices: List<Double>,
    val openPrices: List<Double>,
    val highPrices: List<Double>,
    val lowPrices: List<Double>,
    val volumes: List<Long>,
)

data class NetworkSearchItem(
    val symbol: String,
    val displaySymbol: String,
    val description: String,
    val type: String,
)

data class NetworkCompanyProfile(
    val name: String,
    val ticker: String,
    val exchange: String,
    val industry: String,
    val logoUrl: String,
    val marketCap: Double,
    val currency: String,
    val country: String,
    val ipoDate: String,
)

// ── Mapper functions ──────────────────────────────────────────────────────────

/**
 * Maps a [QuoteDto] to [NetworkQuote] for a given [symbol].
 * Nullable API fields default to 0.0 so downstream code stays null-free.
 */
fun QuoteDto.toNetworkQuote(symbol: String) = NetworkQuote(
    symbol        = symbol,
    currentPrice  = currentPrice,
    change        = change ?: 0.0,
    percentChange = percentChange ?: 0.0,
    highPrice     = highPrice,
    lowPrice      = lowPrice,
    openPrice     = openPrice,
    previousClose = previousClose,
    timestamp     = timestamp,
)

/**
 * Maps a [CandleDto] to [NetworkCandle].
 *
 * Returns `null` when the API responds with `status = "no_data"` or when any
 * parallel array is missing — the repository treats `null` as an empty range.
 */
fun CandleDto.toNetworkCandleOrNull(): NetworkCandle? {
    if (status != "ok") return null
    val timestamps  = timestamps  ?: return null
    val close       = closePrices ?: return null
    val open        = openPrices  ?: return null
    val high        = highPrices  ?: return null
    val low         = lowPrices   ?: return null
    val volumes     = volumes     ?: return null

    // Finnhub guarantees parallel arrays of equal length, but guard defensively.
    val size = minOf(timestamps.size, close.size, open.size, high.size, low.size, volumes.size)
    return NetworkCandle(
        timestamps  = timestamps.take(size),
        closePrices = close.take(size),
        openPrices  = open.take(size),
        highPrices  = high.take(size),
        lowPrices   = low.take(size),
        volumes     = volumes.take(size),
    )
}

fun SearchItemDto.toNetworkSearchItem() = NetworkSearchItem(
    symbol        = symbol,
    displaySymbol = displaySymbol,
    description   = description,
    type          = type,
)

fun CompanyProfileDto.toNetworkCompanyProfile() = NetworkCompanyProfile(
    name      = name.orEmpty(),
    ticker    = ticker.orEmpty(),
    exchange  = exchange.orEmpty(),
    industry  = industry.orEmpty(),
    logoUrl   = logoUrl.orEmpty(),
    marketCap = marketCap ?: 0.0,
    currency  = currency.orEmpty(),
    country   = country.orEmpty(),
    ipoDate   = ipoDate.orEmpty(),
)
