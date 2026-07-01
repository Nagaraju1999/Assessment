package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.StockSearchResult
import com.nagaraju.stocktracker.domain.repository.StockRepository
import com.nagaraju.stocktracker.domain.usecase.watchlist.SearchStocksUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchStocksUseCaseTest {

    private lateinit var stockRepository: StockRepository
    private lateinit var useCase: SearchStocksUseCase

    @Before
    fun setUp() {
        stockRepository = mockk()
        useCase = SearchStocksUseCase(stockRepository)
    }

    @Test
    fun `invoke returns empty success for blank query without calling repository`() = runTest {
        val result = useCase("   ")

        assertTrue(result is Result.Success)
        assertEquals(emptyList<StockSearchResult>(), (result as Result.Success).data)
        coVerify(exactly = 0) { stockRepository.searchStocks(any()) }
    }

    @Test
    fun `invoke delegates to repository for non blank query`() = runTest {
        val expected = listOf(
            StockSearchResult("AAPL", "AAPL", "Apple Inc", "Common Stock"),
        )
        coEvery { stockRepository.searchStocks("apple") } returns Result.Success(expected)

        val result = useCase("apple")

        assertEquals(Result.Success(expected), result)
    }

    @Test
    fun `invoke trims whitespace before delegating to repository`() = runTest {
        coEvery { stockRepository.searchStocks("apple") } returns Result.Success(emptyList())

        useCase("  apple  ")

        coVerify { stockRepository.searchStocks("apple") }
    }
}
