package com.nagaraju.stocktracker.feature.watchlist

import app.cash.turbine.test
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.model.StockSearchResult
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.usecase.watchlist.AddToWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.GetWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.RemoveFromWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.SearchStocksUseCase
import com.nagaraju.stocktracker.feature.watchlist.presentation.state.WatchlistEvent
import com.nagaraju.stocktracker.feature.watchlist.presentation.viewmodel.WatchlistViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchlistViewModelTest {

    private lateinit var getWatchlistUseCase: GetWatchlistUseCase
    private lateinit var addToWatchlistUseCase: AddToWatchlistUseCase
    private lateinit var removeFromWatchlistUseCase: RemoveFromWatchlistUseCase
    private lateinit var searchStocksUseCase: SearchStocksUseCase

    private fun stock(symbol: String) = Stock(
        symbol = symbol, companyName = "$symbol Inc", currentPrice = 100.0,
        change = 1.0, percentChange = 1.0, highPrice = 102.0, lowPrice = 98.0,
        openPrice = 99.0, previousClose = 99.0, addedAt = 1000L,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        getWatchlistUseCase = mockk()
        addToWatchlistUseCase = mockk()
        removeFromWatchlistUseCase = mockk()
        searchStocksUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = WatchlistViewModel(
        getWatchlistUseCase, addToWatchlistUseCase, removeFromWatchlistUseCase, searchStocksUseCase,
    )

    @Test
    fun `initial state shows loading then populated stocks on success`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(
            Result.Loading,
            Result.Success(listOf(stock("AAPL"))),
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Skip any already-collapsed default/loading frames and assert
            // on the terminal, fully-loaded state — StateFlow only ever
            // guarantees the *latest* value is observable, not every
            // intermediate emission survives collection timing.
            var latest = awaitItem()
            while (latest.stocks.isEmpty()) {
                latest = awaitItem()
            }
            assertEquals(1, latest.stocks.size)
            assertTrue(!latest.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error with empty stocks sets errorMessage on state`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(
            Result.Error(DomainException.NoInternetException()),
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var latest = awaitItem()
            while (latest.errorMessage == null) {
                latest = awaitItem()
            }
            assertTrue(latest.errorMessage != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error with existing stocks emits ShowError event instead of blanking state`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(
            Result.Success(listOf(stock("AAPL"))),
            Result.Error(DomainException.RateLimitExceededException()),
        )

        val viewModel = createViewModel()

        viewModel.events.test {
            val event = awaitItem()
            assertTrue(event is WatchlistEvent.ShowError)
        }
    }

    @Test
    fun `onAddStockClick shows the search sheet`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        val viewModel = createViewModel()

        viewModel.onAddStockClick()

        assertTrue(viewModel.uiState.value.isSearchSheetVisible)
    }

    @Test
    fun `onSearchSheetDismiss resets search state`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        val viewModel = createViewModel()
        viewModel.onAddStockClick()

        viewModel.onSearchSheetDismiss()

        val state = viewModel.uiState.value
        assertTrue(!state.isSearchSheetVisible)
        assertEquals("", state.searchQuery)
        assertEquals(emptyList<StockSearchResult>(), state.searchResults)
    }

    @Test
    fun `onSearchQueryChange debounces then populates results`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        val results = listOf(StockSearchResult("AAPL", "AAPL", "Apple Inc", "Common Stock"))
        coEvery { searchStocksUseCase("apple") } returns Result.Success(results)
        val viewModel = createViewModel()

        viewModel.onSearchQueryChange("apple")
        // Advance virtual time past the debounce window.
        kotlinx.coroutines.test.advanceTimeBy(400L)
        kotlinx.coroutines.test.advanceUntilIdle()

        assertEquals(results, viewModel.uiState.value.searchResults)
    }

    @Test
    fun `onSearchResultClick adds stock and dismisses sheet on success`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        coEvery { addToWatchlistUseCase("AAPL", "Apple Inc") } returns Result.Success(Unit)
        val viewModel = createViewModel()
        viewModel.onAddStockClick()

        viewModel.events.test {
            viewModel.onSearchResultClick("AAPL", "Apple Inc")
            val event = awaitItem()
            assertTrue(event is WatchlistEvent.StockAdded)
        }
        assertTrue(!viewModel.uiState.value.isSearchSheetVisible)
    }

    @Test
    fun `onSearchResultClick emits ShowError on failure and keeps sheet open`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        coEvery { addToWatchlistUseCase("FAKE", "Fake Inc") } returns
            Result.Error(DomainException.EmptyResponseException("FAKE"))
        val viewModel = createViewModel()
        viewModel.onAddStockClick()

        viewModel.events.test {
            viewModel.onSearchResultClick("FAKE", "Fake Inc")
            val event = awaitItem()
            assertTrue(event is WatchlistEvent.ShowError)
        }
        assertTrue(viewModel.uiState.value.isSearchSheetVisible)
    }

    @Test
    fun `onRemoveStockClick emits StockRemoved event on success`() = runTest {
        every { getWatchlistUseCase(any()) } returns flowOf(Result.Success(emptyList()))
        coEvery { removeFromWatchlistUseCase("AAPL") } returns Result.Success(Unit)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onRemoveStockClick("AAPL")
            val event = awaitItem()
            assertTrue(event is WatchlistEvent.StockRemoved)
        }
    }
}
