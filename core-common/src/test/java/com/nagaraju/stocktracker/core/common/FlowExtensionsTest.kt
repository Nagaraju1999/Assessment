package com.nagaraju.stocktracker.core.common

import app.cash.turbine.test
import com.nagaraju.stocktracker.core.common.extensions.asResult
import com.nagaraju.stocktracker.core.common.extensions.pollingFlow
import com.nagaraju.stocktracker.domain.result.Result
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowExtensionsTest {

    // ── asResult ──────────────────────────────────────────────────────────────

    @Test
    fun `asResult emits Loading then Success`() = runTest {
        flowOf(42)
            .asResult()
            .test {
                assertEquals(Result.Loading, awaitItem())
                assertEquals(Result.Success(42), awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun `asResult emits Loading then Error when upstream throws`() = runTest {
        val exception = RuntimeException("network error")
        flow<Int> { throw exception }
            .asResult()
            .test {
                assertEquals(Result.Loading, awaitItem())
                val error = awaitItem()
                assertTrue(error is Result.Error)
                assertEquals(exception, (error as Result.Error).exception)
                awaitComplete()
            }
    }

    @Test
    fun `asResult wraps multiple emissions correctly`() = runTest {
        flowOf(1, 2, 3)
            .asResult()
            .test {
                assertEquals(Result.Loading, awaitItem())
                assertEquals(Result.Success(1), awaitItem())
                assertEquals(Result.Success(2), awaitItem())
                assertEquals(Result.Success(3), awaitItem())
                awaitComplete()
            }
    }

    // ── pollingFlow ───────────────────────────────────────────────────────────

    @Test
    fun `pollingFlow emits immediately then repeats`() = runTest {
        var callCount = 0
        pollingFlow(intervalMillis = 100L) { ++callCount }
            .test {
                assertEquals(1, awaitItem()) // immediate first emission
                assertEquals(2, awaitItem()) // after 100 ms
                cancel()
            }
    }
}
