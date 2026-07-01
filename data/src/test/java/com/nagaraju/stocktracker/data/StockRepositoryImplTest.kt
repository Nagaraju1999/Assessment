package com.nagaraju.stocktracker.data

import app.cash.turbine.test
import com.nagaraju.stocktracker.core.common.dispatcher.DispatcherProvider
import com.nagaraju.stocktracker.core.common.network.NetworkMonitor
import com.nagaraju.stocktracker.core.network.mapper.NetworkCompanyProfile
import com.nagaraju.stocktracker.core.network.mapper.NetworkQuote
import com.nagaraju.stocktracker.core.network.mapper.NetworkSearchItem
import com.nagaraju.stocktracker.data.repository.StockRepositoryImpl
import com.nagaraju.stocktracker.data.source.local.StockLocalSource
import com.nagaraju.stocktracker.data.source.remote.StockRemoteSource
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StockRepositoryImplTest {

    private lateinit var remoteSource: StockRemoteSource
    private lateinit var localSource: StockLocalSource
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var repository: StockRepositoryImpl

    private fun quote(symbol: String, price: Double) = NetworkQuote(
        symbol = symbol, currentPrice = price, change = 1.0, percentChange = 0.5,
        highPrice = price + 2, lowPrice = price - 2, openPrice = price - 1,
        previousClose = price - 1, timestamp = 1000L,
    )

    @Before
    fun setUp() {
        remoteSource = mockk()
        localSource = mockk()
        networkMonitor = mockk()
        val testDispatcher = StandardTestDispatcher()
        dispatcherProvider = mockk {
            every { io } returns testDispatcher
            every { main } returns testDispatcher
            every { default } returns testDispatcher
        }
        repository = StockRepositoryImpl(remoteSource, localSource, networkMonitor, dispatcherProvider)
    }

    // ── addToWatchlist ───────────────────────────────────────────────────────

    @Test
    fun `addToWatchlist returns NoInternetException when offline`() = runTest {
        every { networkMonitor.isConnected } returns flowOf(false)

        val result = repository.addToWatchlist("AAPL", "Apple Inc")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is DomainException.NoInternetException)
        coVerify(exactly = 0) { remoteSource.getQuote(any()) }
    }

    @Test
    fun `addToWatchlist validates symbol via quote then persists`() = runTest {
        every { networkMonitor.isConnected } returns flowOf(true)
        coEvery { remoteSource.getQuote("AAPL") } returns quote("AAPL", 182.0)
        coEvery { localSource.addToWatchlist(any()) } returns Unit

        val result = repository.addToWatchlist("AAPL", "Apple Inc")

        assertTrue(result is Result.Success)
        coVerify { localSource.addToWatchlist(match { it.symbol == "AAPL" && it.companyName == "Apple Inc" }) }
    }

    @Test
    fun `addToWatchlist returns Error when symbol does not resolve to a quote`() = runTest {
        every { networkMonitor.isConnected } returns flowOf(true)
        coEvery { remoteSource.getQuote("FAKE") } throws DomainException.EmptyResponseException("FAKE")

        val result = repository.addToWatchlist("FAKE", "Fake Inc")

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { localSource.addToWatchlist(any()) }
    }

    // ── removeFromWatchlist ──────────────────────────────────────────────────

    @Test
    fun `removeFromWatchlist delegates to local source`() = runTest {
        coEvery { localSource.removeFromWatchlist("AAPL") } returns Unit

        val result = repository.removeFromWatchlist("AAPL")

        assertTrue(result is Result.Success)
        coVerify { localSource.removeFromWatchlist("AAPL") }
    }

    // ── searchStocks ─────────────────────────────────────────────────────────

    @Test
    fun `searchStocks returns NoInternetException when offline`() = runTest {
        every { networkMonitor.isConnected } returns flowOf(false)

        val result = repository.searchStocks("apple")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is DomainException.NoInternetException)
    }

    @Test
    fun `searchStocks delegates to remote source when online`() = runTest {
        every { networkMonitor.isConnected } returns flowOf(true)
        coEvery { remoteSource.searchSymbols("apple") } returns listOf(
            NetworkSearchItem("AAPL", "AAPL", "Apple Inc", "Common Stock"),
        )

        val result = repository.searchStocks("apple")

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
    }

    // ── getStockHistory ──────────────────────────────────────────────────────

    @Test
    fun `getStockHistory returns EmptyResponseException when candles are null`() = runTest {
        coEvery { remoteSource.getCandles("AAPL", TimeRange.ONE_DAY) } returns null

        val result = repository.getStockHistory("AAPL", TimeRange.ONE_DAY)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is DomainException.EmptyResponseException)
    }

    // ── getCompanyProfile ────────────────────────────────────────────────────

    @Test
    fun `getCompanyProfile maps remote profile to domain model with symbol attached`() = runTest {
        coEvery { remoteSource.getCompanyProfile("AAPL") } returns NetworkCompanyProfile(
            name = "Apple Inc", ticker = "AAPL", exchange = "NASDAQ", industry = "Technology",
            logoUrl = "https://logo.png", marketCap = 2_800_000.0, currency = "USD", country = "US",
            ipoDate = "1980-12-12",
        )

        val result = repository.getCompanyProfile("AAPL")

        assertTrue(result is Result.Success)
        assertEquals("AAPL", (result as Result.Success).data.symbol)
    }

    // ── isInWatchlist ────────────────────────────────────────────────────────

    @Test
    fun `isInWatchlist delegates to local source`() = runTest {
        every { localSource.observeIsInWatchlist("AAPL") } returns flowOf(true)

        repository.isInWatchlist("AAPL").test {
            assertEquals(true, awaitItem())
            cancel()
        }
    }
}
