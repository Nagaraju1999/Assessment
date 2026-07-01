package com.nagaraju.stocktracker.domain

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import com.nagaraju.stocktracker.domain.usecase.alert.CreateAlertUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateAlertUseCaseTest {

    private lateinit var alertRepository: AlertRepository
    private lateinit var useCase: CreateAlertUseCase

    @Before
    fun setUp() {
        alertRepository = mockk()
        useCase = CreateAlertUseCase(alertRepository)
    }

    @Test
    fun `invoke returns Error for zero target price without calling repository`() = runTest {
        val result = useCase("AAPL", AlertCondition.ABOVE, 0.0)

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { alertRepository.createAlert(any(), any(), any()) }
    }

    @Test
    fun `invoke returns Error for negative target price`() = runTest {
        val result = useCase("AAPL", AlertCondition.BELOW, -10.0)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `invoke trims and uppercases symbol before calling repository`() = runTest {
        coEvery {
            alertRepository.createAlert("AAPL", AlertCondition.ABOVE, 200.0)
        } returns Result.Success(1L)

        useCase("  aapl  ", AlertCondition.ABOVE, 200.0)

        coVerify { alertRepository.createAlert("AAPL", AlertCondition.ABOVE, 200.0) }
    }

    @Test
    fun `invoke returns repository result for valid input`() = runTest {
        coEvery {
            alertRepository.createAlert("AAPL", AlertCondition.ABOVE, 200.0)
        } returns Result.Success(42L)

        val result = useCase("AAPL", AlertCondition.ABOVE, 200.0)

        assertTrue(result is Result.Success)
        assertEquals(42L, (result as Result.Success).data)
    }
}
