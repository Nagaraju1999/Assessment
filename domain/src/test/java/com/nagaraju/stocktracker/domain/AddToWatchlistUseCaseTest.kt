package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.repository.StockRepository
import com.nagaraju.stocktracker.domain.usecase.watchlist.AddToWatchlistUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddToWatchlistUseCaseTest {

    private lateinit var stockRepository: StockRepository
    private lateinit var useCase: AddToWatchlistUseCase

    @Before
    fun setUp() {
        stockRepository = mockk()
        useCase = AddToWatchlistUseCase(stockRepository)
    }

    @Test
    fun `invoke returns Error for blank symbol without calling repository`() = runTest {
        val result = useCase("   ", "Apple Inc")

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { stockRepository.addToWatchlist(any(), any()) }
    }

    @Test
    fun `invoke returns Error for empty symbol`() = runTest {
        val result = useCase("", "Apple Inc")

        assertTrue(result is Result.Error)
    }

    @Test
    fun `invoke trims and uppercases the symbol before calling repository`() = runTest {
        coEvery { stockRepository.addToWatchlist("AAPL", "Apple Inc") } returns Result.Success(Unit)

        useCase("  aapl  ", "Apple Inc")

        coVerify { stockRepository.addToWatchlist("AAPL", "Apple Inc") }
    }

    @Test
    fun `invoke trims the company name before calling repository`() = runTest {
        coEvery { stockRepository.addToWatchlist("AAPL", "Apple Inc") } returns Result.Success(Unit)

        useCase("AAPL", "  Apple Inc  ")

        coVerify { stockRepository.addToWatchlist("AAPL", "Apple Inc") }
    }

    @Test
    fun `invoke returns repository result on success`() = runTest {
        coEvery { stockRepository.addToWatchlist("AAPL", "Apple Inc") } returns Result.Success(Unit)

        val result = useCase("AAPL", "Apple Inc")

        assertTrue(result is Result.Success)
    }
}
