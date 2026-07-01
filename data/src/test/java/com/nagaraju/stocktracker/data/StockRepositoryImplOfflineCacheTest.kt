package com.nagaraju.stocktracker.data

import app.cash.turbine.test
import com.nagaraju.stocktracker.core.common.dispatcher.DispatcherProvider
import com.nagaraju.stocktracker.core.common.network.NetworkMonitor
import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity
import com.nagaraju.stocktracker.core.network.mapper.NetworkQuote
import com.nagaraju.stocktracker.data.repository.StockRepositoryImpl
import com.nagaraju.stocktracker.data.source.local.StockLocalSource
import com.nagaraju.stocktracker.data.source.remote.StockRemoteSource
import com.nagaraju.stocktracker.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StockRepositoryImplOfflineCacheTest {

    private lateinit var remoteSource: StockRemoteSource
    private lateinit var localSource: StockLocalSource
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var repository: StockRepositoryImpl

    private fun quote(price: Double = 182.0) = NetworkQuote(
        symbol = "AAPL", currentPrice = price, change = 1.0, percentChange = 0.5,
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

        coEvery {
            localSource.updateCachedQuote(any(), any(), any(), any(), any())
        } just runs
    }

    @Test
    fun `successful poll writes the quote into the cache columns`() = runTest {
        val entity = WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L)
        every { localSource.observeWatchlist() } returns flowOf(listOf(entity))
        coEvery { remoteSource.getQuote("AAPL") } returns quote(182.0)

        repository.observeWatchlist(pollIntervalMillis = 1_000_000L).test {
            skipItems(1) // Result.Loading
            val success = awaitItem()
            assertTrue(success is Result.Success)
            cancel()
        }

        coVerify {
            localSource.updateCachedQuote(
                symbol = "AAPL",
                price = 182.0,
                change = 1.0,
                percentChange = 0.5,
                cachedAt = any(),
            )
        }
    }

    @Test
    fun `failed poll with a prior cache returns the cached price marked isCached`() = runTest {
        val entity = WatchlistEntity(
            symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L,
            cachedPrice = 178.5, cachedChange = -2.0, cachedPercentChange = -1.1, cachedAt = 5000L,
        )
        every { localSource.observeWatchlist() } returns flowOf(listOf(entity))
        coEvery { remoteSource.getQuote("AAPL") } throws RuntimeException("network down")

        repository.observeWatchlist(pollIntervalMillis = 1_000_000L).test {
            skipItems(1) // Result.Loading
            val success = awaitItem() as Result.Success
            val stock = success.data.first()

            assertTrue(stock.isCached)
            assertEquals(178.5, stock.currentPrice, 0.001)
            assertEquals(-2.0, stock.change, 0.001)
            assertEquals(5000L, stock.priceTimestamp)
            cancel()
        }
    }

    @Test
    fun `failed poll with no prior cache returns a zeroed identity-only stock`() = runTest {
        val entity = WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L)
        every { localSource.observeWatchlist() } returns flowOf(listOf(entity))
        coEvery { remoteSource.getQuote("AAPL") } throws RuntimeException("network down")

        repository.observeWatchlist(pollIntervalMillis = 1_000_000L).test {
            skipItems(1) // Result.Loading
            val success = awaitItem() as Result.Success
            val stock = success.data.first()

            assertTrue(!stock.isCached)
            assertEquals(0.0, stock.currentPrice, 0.001)
            assertEquals("Apple Inc", stock.companyName)
            cancel()
        }
    }

    @Test
    fun `one symbol failing does not affect another symbol succeeding in the same poll`() = runTest {
        val applEntity = WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L)
        val teslaEntity = WatchlistEntity(
            symbol = "TSLA", companyName = "Tesla Inc", addedAt = 2000L,
            cachedPrice = 240.0, cachedChange = 1.0, cachedPercentChange = 0.4, cachedAt = 3000L,
        )
        every { localSource.observeWatchlist() } returns flowOf(listOf(applEntity, teslaEntity))
        coEvery { remoteSource.getQuote("AAPL") } returns quote(182.0)
        coEvery { remoteSource.getQuote("TSLA") } throws RuntimeException("rate limited")

        repository.observeWatchlist(pollIntervalMillis = 1_000_000L).test {
            skipItems(1)
            val success = awaitItem() as Result.Success
            val stocks = success.data

            val apple = stocks.first { it.symbol == "AAPL" }
            val tesla = stocks.first { it.symbol == "TSLA" }

            assertTrue(!apple.isCached)
            assertEquals(182.0, apple.currentPrice, 0.001)

            assertTrue(tesla.isCached)
            assertEquals(240.0, tesla.currentPrice, 0.001)
            cancel()
        }
    }
}
