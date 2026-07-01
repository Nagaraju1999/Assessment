package com.nagaraju.stocktracker.core.network

import com.nagaraju.stocktracker.core.network.dto.CandleDto
import com.nagaraju.stocktracker.core.network.dto.CompanyProfileDto
import com.nagaraju.stocktracker.core.network.dto.QuoteDto
import com.nagaraju.stocktracker.core.network.dto.SearchItemDto
import com.nagaraju.stocktracker.core.network.mapper.toNetworkCandleOrNull
import com.nagaraju.stocktracker.core.network.mapper.toNetworkCompanyProfile
import com.nagaraju.stocktracker.core.network.mapper.toNetworkQuote
import com.nagaraju.stocktracker.core.network.mapper.toNetworkSearchItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkMapperTest {

    // ── QuoteDto ──────────────────────────────────────────────────────────────

    @Test
    fun `toNetworkQuote maps all fields correctly`() {
        val dto = QuoteDto(
            currentPrice  = 182.34,
            change        = 1.23,
            percentChange = 0.68,
            highPrice     = 184.0,
            lowPrice      = 180.0,
            openPrice     = 181.5,
            previousClose = 181.11,
            timestamp     = 1718000000L,
        )

        val result = dto.toNetworkQuote("AAPL")

        assertEquals("AAPL",      result.symbol)
        assertEquals(182.34,      result.currentPrice,  0.001)
        assertEquals(1.23,        result.change,        0.001)
        assertEquals(0.68,        result.percentChange, 0.001)
        assertEquals(184.0,       result.highPrice,     0.001)
        assertEquals(180.0,       result.lowPrice,      0.001)
        assertEquals(181.5,       result.openPrice,     0.001)
        assertEquals(181.11,      result.previousClose, 0.001)
        assertEquals(1718000000L, result.timestamp)
    }

    @Test
    fun `toNetworkQuote defaults nullable change fields to 0`() {
        val dto = QuoteDto(
            currentPrice  = 50.0,
            change        = null,
            percentChange = null,
            highPrice     = 51.0,
            lowPrice      = 49.0,
            openPrice     = 50.5,
            previousClose = 50.0,
            timestamp     = 1718000000L,
        )

        val result = dto.toNetworkQuote("TSLA")

        assertEquals(0.0, result.change,        0.001)
        assertEquals(0.0, result.percentChange, 0.001)
    }

    // ── CandleDto ─────────────────────────────────────────────────────────────

    @Test
    fun `toNetworkCandleOrNull returns candle for ok status`() {
        val dto = CandleDto(
            closePrices = listOf(180.0, 181.0, 182.0),
            highPrices  = listOf(183.0, 184.0, 185.0),
            lowPrices   = listOf(179.0, 180.0, 181.0),
            openPrices  = listOf(180.5, 181.5, 182.5),
            volumes     = listOf(1000L, 2000L, 3000L),
            timestamps  = listOf(1000L, 2000L, 3000L),
            status      = "ok",
        )

        val result = dto.toNetworkCandleOrNull()

        assertNotNull(result)
        assertEquals(3, result!!.timestamps.size)
        assertEquals(182.0, result.closePrices[2], 0.001)
        assertEquals(3000L, result.volumes[2])
    }

    @Test
    fun `toNetworkCandleOrNull returns null for no_data status`() {
        val dto = CandleDto(
            closePrices = null,
            highPrices  = null,
            lowPrices   = null,
            openPrices  = null,
            volumes     = null,
            timestamps  = null,
            status      = "no_data",
        )

        assertNull(dto.toNetworkCandleOrNull())
    }

    @Test
    fun `toNetworkCandleOrNull returns null when any parallel array is missing`() {
        val dto = CandleDto(
            closePrices = listOf(180.0),
            highPrices  = null,           // missing — should return null
            lowPrices   = listOf(179.0),
            openPrices  = listOf(179.5),
            volumes     = listOf(1000L),
            timestamps  = listOf(1000L),
            status      = "ok",
        )

        assertNull(dto.toNetworkCandleOrNull())
    }

    @Test
    fun `toNetworkCandleOrNull trims to shortest array length`() {
        val dto = CandleDto(
            closePrices = listOf(1.0, 2.0, 3.0),
            highPrices  = listOf(1.5, 2.5, 3.5),
            lowPrices   = listOf(0.5, 1.5, 2.5),
            openPrices  = listOf(1.0, 2.0),          // shorter — should trim others
            volumes     = listOf(100L, 200L, 300L),
            timestamps  = listOf(1000L, 2000L, 3000L),
            status      = "ok",
        )

        val result = dto.toNetworkCandleOrNull()

        assertNotNull(result)
        assertEquals(2, result!!.timestamps.size)
    }

    // ── SearchItemDto ─────────────────────────────────────────────────────────

    @Test
    fun `toNetworkSearchItem maps all fields`() {
        val dto = SearchItemDto(
            description   = "Apple Inc",
            displaySymbol = "AAPL",
            symbol        = "AAPL",
            type          = "Common Stock",
        )

        val result = dto.toNetworkSearchItem()

        assertEquals("Apple Inc",    result.description)
        assertEquals("AAPL",         result.symbol)
        assertEquals("Common Stock", result.type)
    }

    // ── CompanyProfileDto ─────────────────────────────────────────────────────

    @Test
    fun `toNetworkCompanyProfile defaults null strings to empty`() {
        val dto = CompanyProfileDto(
            name      = null,
            ticker    = "AAPL",
            exchange  = null,
            industry  = null,
            logoUrl   = null,
            webUrl    = null,
            marketCap = null,
            sharesOutstanding = null,
            currency  = null,
            country   = null,
            ipoDate   = null,
        )

        val result = dto.toNetworkCompanyProfile()

        assertEquals("",     result.name)
        assertEquals("AAPL", result.ticker)
        assertEquals("",     result.exchange)
        assertEquals(0.0,    result.marketCap, 0.001)
    }

    @Test
    fun `toNetworkCompanyProfile maps populated fields correctly`() {
        val dto = CompanyProfileDto(
            name      = "Apple Inc",
            ticker    = "AAPL",
            exchange  = "NASDAQ",
            industry  = "Technology",
            logoUrl   = "https://logo.example.com/aapl.png",
            webUrl    = "https://apple.com",
            marketCap = 2_800_000.0,
            sharesOutstanding = 15_500.0,
            currency  = "USD",
            country   = "US",
            ipoDate   = "1980-12-12",
        )

        val result = dto.toNetworkCompanyProfile()

        assertEquals("Apple Inc",   result.name)
        assertEquals("NASDAQ",      result.exchange)
        assertEquals("Technology",  result.industry)
        assertEquals(2_800_000.0,   result.marketCap, 0.001)
    }
}
