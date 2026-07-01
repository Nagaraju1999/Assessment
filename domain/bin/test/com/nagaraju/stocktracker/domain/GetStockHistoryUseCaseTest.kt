package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.domain.repository.StockRepository
import com.nagaraju.stocktracker.domain.usecase.stock.GetStockHistoryUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetStockHistoryUseCaseTest {

    private lateinit var stockRepository: StockRepository
    private lateinit var useCase: GetStockHistoryUseCase

    @Before
    fun setUp() {
        stockRepository = mockk()
        useCase = GetStockHistoryUseCase(stockRepository)
    }

    @Test
    fun `invoke converts empty candle list into EmptyResponseException`() = runTest {
        val emptyHistory = StockHistory(symbol = "AAPL", range = TimeRange.ONE_DAY, candles = emptyList())
        coEvery { stockRepository.getStockHistory("AAPL", TimeRange.ONE_DAY) } returns
            Result.Success(emptyHistory)

        val result = useCase("AAPL", TimeRange.ONE_DAY)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is DomainException.EmptyResponseException)
    }

    @Test
    fun `invoke passes through Success when candles are present`() = runTest {
        val history = StockHistory(
            symbol  = "AAPL",
            range   = TimeRange.ONE_DAY,
            candles = listOf(
                com.nagaraju.stocktracker.domain.model.StockCandle(
                    timestamp = 1000L, open = 180.0, high = 182.0, low = 179.0, close = 181.0, volume = 1000L,
                ),
            ),
        )
        coEvery { stockRepository.getStockHistory("AAPL", TimeRange.ONE_DAY) } returns Result.Success(history)

        val result = useCase("AAPL", TimeRange.ONE_DAY)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.candles.isNotEmpty())
    }

    @Test
    fun `invoke passes through Error unchanged`() = runTest {
        val exception = DomainException.NoInternetException()
        coEvery { stockRepository.getStockHistory("AAPL", TimeRange.ONE_DAY) } returns Result.Error(exception)

        val result = useCase("AAPL", TimeRange.ONE_DAY)

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is DomainException.NoInternetException)
    }
}
