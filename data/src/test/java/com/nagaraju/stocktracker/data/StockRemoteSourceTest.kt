package com.nagaraju.stocktracker.data

import com.nagaraju.stocktracker.core.network.api.FinnhubApi
import com.nagaraju.stocktracker.core.network.dto.CandleDto
import com.nagaraju.stocktracker.core.network.dto.CompanyProfileDto
import com.nagaraju.stocktracker.core.network.dto.QuoteDto
import com.nagaraju.stocktracker.core.network.dto.SearchItemDto
import com.nagaraju.stocktracker.core.network.dto.SearchResultDto
import com.nagaraju.stocktracker.core.network.interceptor.RateLimitException
import com.nagaraju.stocktracker.core.network.interceptor.ServerException
import com.nagaraju.stocktracker.core.network.interceptor.UnauthorizedException
import com.nagaraju.stocktracker.data.source.remote.StockRemoteSource
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.TimeRange
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class StockRemoteSourceTest {

    private lateinit var api: FinnhubApi
    private lateinit var remoteSource: StockRemoteSource

    @Before
    fun setUp() {
        api = mockk()
        remoteSource = StockRemoteSource(api)
    }

    // ── getQuote ──────────────────────────────────────────────────────────────

    @Test
    fun `getQuote maps DTO to network model with the requested symbol`() = runTest {
        coEvery { api.getQuote("AAPL") } returns QuoteDto(
            currentPrice = 182.34, change = 1.23, percentChange = 0.68,
            highPrice = 184.0, lowPrice = 180.0, openPrice = 181.5,
            previousClose = 181.11, timestamp = 1718000000L,
        )

        val result = remoteSource.getQuote("AAPL")

        assertEquals("AAPL", result.symbol)
        assertEquals(182.34, result.currentPrice, 0.001)
    }

    @Test
    fun `getQuote translates UnauthorizedException to DomainException UnauthorizedException`() = runTest {
        coEvery { api.getQuote("AAPL") } throws UnauthorizedException("bad key")

        val exception = runCatching { remoteSource.getQuote("AAPL") }.exceptionOrNull()

        assertTrue(exception is DomainException.UnauthorizedException)
    }

    @Test
    fun `getQuote translates RateLimitException to DomainException RateLimitExceededException`() = runTest {
        coEvery { api.getQuote("AAPL") } throws RateLimitException("slow down")

        val exception = runCatching { remoteSource.getQuote("AAPL") }.exceptionOrNull()

        assertTrue(exception is DomainException.RateLimitExceededException)
    }

    @Test
    fun `getQuote translates ServerException to DomainException ServerException`() = runTest {
        coEvery { api.getQuote("AAPL") } throws ServerException("down")

        val exception = runCatching { remoteSource.getQuote("AAPL") }.exceptionOrNull()

        assertTrue(exception is DomainException.ServerException)
    }

    @Test
    fun `getQuote translates SocketTimeoutException to DomainException TimeoutException`() = runTest {
        coEvery { api.getQuote("AAPL") } throws SocketTimeoutException("timeout")

        val exception = runCatching { remoteSource.getQuote("AAPL") }.exceptionOrNull()

        assertTrue(exception is DomainException.TimeoutException)
    }

    @Test
    fun `getQuote translates UnknownHostException to DomainException NoInternetException`() = runTest {
        coEvery { api.getQuote("AAPL") } throws UnknownHostException("no dns")

        val exception = runCatching { remoteSource.getQuote("AAPL") }.exceptionOrNull()

        assertTrue(exception is DomainException.NoInternetException)
    }

    @Test
    fun `getQuote translates unrecognized exceptions to DomainException UnknownException`() = runTest {
        coEvery { api.getQuote("AAPL") } throws IllegalStateException("weird")

        val exception = runCatching { remoteSource.getQuote("AAPL") }.exceptionOrNull()

        assertTrue(exception is DomainException.UnknownException)
    }

    // ── getCandles ────────────────────────────────────────────────────────────

    @Test
    fun `getCandles returns null for no_data status`() = runTest {
        coEvery {
            api.getCandles("AAPL", any(), any(), any())
        } returns CandleDto(null, null, null, null, null, null, "no_data")

        val result = remoteSource.getCandles("AAPL", TimeRange.ONE_DAY)

        assertEquals(null, result)
    }

    @Test
    fun `getCandles returns mapped data for ok status`() = runTest {
        coEvery {
            api.getCandles("AAPL", any(), any(), any())
        } returns CandleDto(
            closePrices = listOf(180.0), highPrices = listOf(182.0),
            lowPrices   = listOf(178.0), openPrices  = listOf(179.0),
            volumes     = listOf(1000L), timestamps  = listOf(1000L),
            status      = "ok",
        )

        val result = remoteSource.getCandles("AAPL", TimeRange.ONE_DAY)

        assertTrue(result != null)
        assertEquals(1, result!!.timestamps.size)
    }

    // ── searchSymbols ─────────────────────────────────────────────────────────

    @Test
    fun `searchSymbols maps every result item`() = runTest {
        coEvery { api.searchSymbol("apple") } returns SearchResultDto(
            count   = 1,
            results = listOf(SearchItemDto("Apple Inc", "AAPL", "AAPL", "Common Stock")),
        )

        val result = remoteSource.searchSymbols("apple")

        assertEquals(1, result.size)
        assertEquals("AAPL", result[0].symbol)
    }

    // ── getCompanyProfile ─────────────────────────────────────────────────────

    @Test
    fun `getCompanyProfile maps DTO to network model`() = runTest {
        coEvery { api.getCompanyProfile("AAPL") } returns CompanyProfileDto(
            name = "Apple Inc", ticker = "AAPL", exchange = "NASDAQ",
            industry = "Technology", logo = "https://logo.png", weburl = "https://apple.com",
            marketCapitalization = 2_800_000.0, shareOutstanding = 15_500.0,
            currency = "USD", country = "US", ipo = "1980-12-12",
        )

        val result = remoteSource.getCompanyProfile("AAPL")

        assertEquals("Apple Inc", result.name)
        assertEquals("NASDAQ",    result.exchange)
    }
}
