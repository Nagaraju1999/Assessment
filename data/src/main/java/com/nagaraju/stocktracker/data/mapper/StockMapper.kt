package com.nagaraju.stocktracker.data.mapper

import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity
import com.nagaraju.stocktracker.core.network.mapper.NetworkCandle
import com.nagaraju.stocktracker.core.network.mapper.NetworkCompanyProfile
import com.nagaraju.stocktracker.core.network.mapper.NetworkQuote
import com.nagaraju.stocktracker.core.network.mapper.NetworkSearchItem
import com.nagaraju.stocktracker.domain.model.CompanyProfile
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.model.StockCandle
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.StockSearchResult
import com.nagaraju.stocktracker.domain.model.TimeRange

/**
 * Combines a [WatchlistEntity] (persisted identity) with a [NetworkQuote]
 * (live price data) into the domain [Stock] model the presentation layer
 * consumes.
 */
fun WatchlistEntity.toDomain(quote: NetworkQuote): Stock = Stock(
    symbol         = symbol,
    companyName    = companyName,
    currentPrice   = quote.currentPrice,
    change         = quote.change,
    percentChange  = quote.percentChange,
    highPrice      = quote.highPrice,
    lowPrice       = quote.lowPrice,
    openPrice      = quote.openPrice,
    previousClose  = quote.previousClose,
    addedAt        = addedAt,
    isCached       = false,
    priceTimestamp = quote.timestamp,
)

/** Maps a [Stock] back into a [WatchlistEntity] for persistence (identity fields only). */
fun Stock.toEntity(): WatchlistEntity = WatchlistEntity(
    symbol      = symbol,
    companyName = companyName,
    addedAt     = addedAt,
)

/**
 * Maps a [NetworkCandle] (parallel-array network shape) into a list of
 * [StockCandle] domain models (one object per bar) wrapped in [StockHistory].
 */
fun NetworkCandle.toDomain(symbol: String, range: TimeRange): StockHistory = StockHistory(
    symbol  = symbol,
    range   = range,
    candles = timestamps.indices.map { i ->
        StockCandle(
            timestamp = timestamps[i],
            open      = openPrices[i],
            high      = highPrices[i],
            low       = lowPrices[i],
            close     = closePrices[i],
            volume    = volumes[i],
        )
    },
)

fun NetworkSearchItem.toDomain(): StockSearchResult = StockSearchResult(
    symbol        = symbol,
    displaySymbol = displaySymbol,
    description   = description,
    type          = type,
)

fun NetworkCompanyProfile.toDomain(symbol: String): CompanyProfile = CompanyProfile(
    symbol    = symbol,
    name      = name,
    exchange  = exchange,
    industry  = industry,
    logoUrl   = logoUrl,
    marketCap = marketCap,
    currency  = currency,
    country   = country,
)
