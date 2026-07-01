package com.nagaraju.stocktracker.data

import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity
import com.nagaraju.stocktracker.core.network.mapper.NetworkCandle
import com.nagaraju.stocktracker.core.network.mapper.NetworkCompanyProfile
import com.nagaraju.stocktracker.core.network.mapper.NetworkQuote
import com.nagaraju.stocktracker.core.network.mapper.NetworkSearchItem
import com.nagaraju.stocktracker.data.mapper.toDomain
import com.nagaraju.stocktracker.data.mapper.toEntity
import com.nagaraju.stocktracker.domain.model.TimeRange
import org.junit.Assert.assertEquals
import org.junit.Test

class StockMapperTest {

    @Test
    fun `WatchlistEntity toDomain combines entity identity with quote price data`() {
        val entity = WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L)
        val quote = NetworkQuote(
            symbol        = "AAPL",
            currentPrice  = 182.34,
            change        = 1.23,
            percentChange = 0.68,
            highPrice     = 184.0,
            lowPrice      = 180.0,
            openPrice     = 181.5,
            previousClose = 181.11,
            timestamp     = 1718000000L,
        )

        val stock = entity.toDomain(quote)

        assertEquals("AAPL",      stock.symbol)
        assertEquals("Apple Inc", stock.companyName)
        assertEquals(182.34,      stock.currentPrice, 0.001)
        assertEquals(1000L,       stock.addedAt)
    }

    @Test
    fun `Stock toEntity preserves identity fields only`() {
        val stock = com.nagaraju.stocktracker.domain.model.Stock(
            symbol         = "AAPL",
            companyName    = "Apple Inc",
            currentPrice   = 182.34,
            change         = 1.23,
            percentChange  = 0.68,
            highPrice      = 184.0,
            lowPrice       = 180.0,
            openPrice      = 181.5,
            previousClose  = 181.11,
            addedAt        = 1000L,
        )

        val entity = stock.toEntity()

        assertEquals("AAPL",      entity.symbol)
        assertEquals("Apple Inc", entity.companyName)
        assertEquals(1000L,       entity.addedAt)
    }

    @Test
    fun `NetworkCandle toDomain builds one StockCandle per index`() {
        val networkCandle = NetworkCandle(
            timestamps  = listOf(1000L, 2000L),
            closePrices = listOf(180.0, 181.0),
            openPrices  = listOf(179.0, 180.0),
            highPrices  = listOf(182.0, 183.0),
            lowPrices   = listOf(178.0, 179.0),
            volumes     = listOf(1000L, 2000L),
        )

        val history = networkCandle.toDomain("AAPL", TimeRange.ONE_DAY)

        assertEquals("AAPL", history.symbol)
        assertEquals(TimeRange.ONE_DAY, history.range)
        assertEquals(2, history.candles.size)
        assertEquals(1000L, history.candles[0].timestamp)
        assertEquals(181.0, history.candles[1].close, 0.001)
    }

    @Test
    fun `NetworkSearchItem toDomain maps all fields`() {
        val item = NetworkSearchItem(
            symbol        = "AAPL",
            displaySymbol = "AAPL",
            description   = "Apple Inc",
            type          = "Common Stock",
        )

        val result = item.toDomain()

        assertEquals("AAPL",         result.symbol)
        assertEquals("Apple Inc",    result.description)
        assertEquals("Common Stock", result.type)
    }

    @Test
    fun `NetworkCompanyProfile toDomain attaches the requested symbol`() {
        val profile = NetworkCompanyProfile(
            name      = "Apple Inc",
            ticker    = "AAPL",
            exchange  = "NASDAQ",
            industry  = "Technology",
            logoUrl   = "https://logo.example.com/aapl.png",
            marketCap = 2_800_000.0,
            currency  = "USD",
            country   = "US",
            ipoDate   = "1980-12-12",
        )

        val result = profile.toDomain("AAPL")

        assertEquals("AAPL",      result.symbol)
        assertEquals("Apple Inc", result.name)
        assertEquals("NASDAQ",    result.exchange)
    }
}
