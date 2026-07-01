package com.nagaraju.stocktracker.feature.stockdetails

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.CompanyProfile
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.model.StockCandle
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.domain.repository.StockRepository
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.usecase.stock.GetCompanyProfileUseCase
import com.nagaraju.stocktracker.domain.usecase.stock.GetStockDetailsUseCase
import com.nagaraju.stocktracker.domain.usecase.stock.GetStockHistoryUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.AddToWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.RemoveFromWatchlistUseCase
import com.nagaraju.stocktracker.feature.stockdetails.navigation.StockDetailsRoutes
import com.nagaraju.stocktracker.feature.stockdetails.presentation.state.StockDetailsEvent
import com.nagaraju.stocktracker.feature.stockdetails.presentation.viewmodel.StockDetailsViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockDetailsViewModelTest {

    private lateinit var getStockDetailsUseCase: GetStockDetailsUseCase
    private lateinit var getStockHistoryUseCase: GetStockHistoryUseCase
    private lateinit var getCompanyProfileUseCase: GetCompanyProfileUseCase
    private lateinit var addToWatchlistUseCase: AddToWatchlistUseCase
    private lateinit var removeFromWatchlistUseCase: RemoveFromWatchlistUseCase
    private lateinit var stockRepository: StockRepository

    private fun stock(symbol: String = "AAPL") = Stock(
        symbol = symbol, companyName = "Apple Inc", currentPrice = 182.0,
        change = 1.0, percentChange = 0.5, highPrice = 184.0, lowPrice = 180.0,
        openPrice = 181.0, previousClose = 181.0, addedAt = 1000L,
    )

    private fun history(range: TimeRange = TimeRange.ONE_DAY, hasCandles: Boolean = true) = StockHistory(
        symbol = "AAPL",
        range = range,
        candles = if (hasCandles) {
            listOf(StockCandle(1000L, 180.0, 182.0, 178.0, 181.0, 1000L))
        } else {
            emptyList()
        },
    )

    private fun createViewModel(symbol: String = "AAPL"): StockDetailsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf(StockDetailsRoutes.SYMBOL_ARG to symbol))
        return StockDetailsViewModel(
            savedStateHandle, getStockDetailsUseCase, getStockHistoryUseCase,
            getCompanyProfileUseCase, addToWatchlistUseCase, removeFromWatchlistUseCase, stockRepository,
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        getStockDetailsUseCase = mockk()
        getStockHistoryUseCase = mockk()
        getCompanyProfileUseCase = mockk()
        addToWatchlistUseCase = mockk()
        removeFromWatchlistUseCase = mockk()
        stockRepository = mockk()

        // Common defaults so each test only overrides what it cares about.
        every { stockRepository.isInWatchlist(any()) } returns flowOf(false)
        coEvery { getStockHistoryUseCase(any(), any()) } returns Result.Success(history())
        coEvery { getCompanyProfileUseCase(any()) } returns Result.Success(
            CompanyProfile("AAPL", "Apple Inc", "NASDAQ", "Technology", "", 0.0, "USD", "US"),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `throws when symbol argument is missing`() {
        val savedStateHandle = SavedStateHandle()
        val exception = runCatching {
            StockDetailsViewModel(
                savedStateHandle, getStockDetailsUseCase, getStockHistoryUseCase,
                getCompanyProfileUseCase, addToWatchlistUseCase, removeFromWatchlistUseCase, stockRepository,
            )
        }.exceptionOrNull()

        assertTrue(exception is IllegalStateException)
    }

    @Test
    fun `loads quote and updates state on success`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("AAPL", viewModel.uiState.value.stock?.symbol)
        assertTrue(!viewModel.uiState.value.isQuoteLoading)
    }

    @Test
    fun `quote error with no prior data sets quoteErrorMessage`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns
            flowOf(Result.Error(DomainException.NoInternetException()))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.quoteErrorMessage != null)
    }

    @Test
    fun `onRangeSelected reloads history for the new range`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        coEvery { getStockHistoryUseCase("AAPL", TimeRange.ONE_WEEK) } returns
            Result.Success(history(range = TimeRange.ONE_WEEK))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRangeSelected(TimeRange.ONE_WEEK)
        advanceUntilIdle()

        assertEquals(TimeRange.ONE_WEEK, viewModel.uiState.value.selectedRange)
        assertEquals(TimeRange.ONE_WEEK, viewModel.uiState.value.history?.range)
    }

    @Test
    fun `onRangeSelected with same range is a no-op`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onRangeSelected(TimeRange.ONE_DAY) // already selected by default
        advanceUntilIdle()

        assertEquals(TimeRange.ONE_DAY, viewModel.uiState.value.selectedRange)
    }

    @Test
    fun `watchlist toggle adds when not currently in watchlist`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        every { stockRepository.isInWatchlist("AAPL") } returns flowOf(false)
        coEvery { addToWatchlistUseCase("AAPL", any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onWatchlistToggleClick()
            val event = awaitItem()
            assertTrue(event is StockDetailsEvent.AddedToWatchlist)
        }
    }

    @Test
    fun `watchlist toggle removes when currently in watchlist`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        every { stockRepository.isInWatchlist("AAPL") } returns flowOf(true)
        coEvery { removeFromWatchlistUseCase("AAPL") } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onWatchlistToggleClick()
            val event = awaitItem()
            assertTrue(event is StockDetailsEvent.RemovedFromWatchlist)
        }
    }

    @Test
    fun `company profile failure does not block the rest of the screen`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        coEvery { getCompanyProfileUseCase("AAPL") } returns
            Result.Error(DomainException.UnknownException())

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Quote still loads successfully even though profile failed silently.
        assertEquals("AAPL", viewModel.uiState.value.stock?.symbol)
        assertEquals(null, viewModel.uiState.value.companyProfile)
    }

    @Test
    fun `priceTrend is null when history has too few candles`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        // The default history() fixture has a single candle, below the
        // 3-candle minimum PriceTrendCalculator requires.
        coEvery { getStockHistoryUseCase(any(), any()) } returns Result.Success(history())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.priceTrend)
    }

    @Test
    fun `priceTrend is populated when history has enough candles`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        val multiCandleHistory = StockHistory(
            symbol = "AAPL",
            range = TimeRange.ONE_DAY,
            candles = listOf(
                StockCandle(1000L, 180.0, 182.0, 178.0, 181.0, 1000L),
                StockCandle(2000L, 181.0, 183.0, 179.0, 182.0, 1000L),
                StockCandle(3000L, 182.0, 184.0, 180.0, 183.0, 1000L),
            ),
        )
        coEvery { getStockHistoryUseCase(any(), any()) } returns Result.Success(multiCandleHistory)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.priceTrend != null)
    }

    @Test
    fun `priceTrend is cleared when history fails to load`() = runTest {
        every { getStockDetailsUseCase("AAPL", any()) } returns flowOf(Result.Success(stock()))
        coEvery { getStockHistoryUseCase(any(), any()) } returns
            Result.Error(DomainException.NoInternetException())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.priceTrend)
    }
}
