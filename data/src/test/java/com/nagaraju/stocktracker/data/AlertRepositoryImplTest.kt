package com.nagaraju.stocktracker.data

import app.cash.turbine.test
import com.nagaraju.stocktracker.core.database.entity.AlertConditionEntity
import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import com.nagaraju.stocktracker.core.network.mapper.NetworkQuote
import com.nagaraju.stocktracker.data.repository.AlertRepositoryImpl
import com.nagaraju.stocktracker.data.source.local.AlertLocalSource
import com.nagaraju.stocktracker.data.source.remote.StockRemoteSource
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.result.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertRepositoryImplTest {

    private lateinit var localSource: AlertLocalSource
    private lateinit var remoteSource: StockRemoteSource
    private lateinit var repository: AlertRepositoryImpl

    private fun entity(
        id: Long = 1L,
        symbol: String = "AAPL",
        condition: AlertConditionEntity = AlertConditionEntity.ABOVE,
        targetPrice: Double = 200.0,
    ) = AlertEntity(
        id = id, symbol = symbol, condition = condition, targetPrice = targetPrice,
        isEnabled = true, isTriggered = false, createdAt = 1000L,
    )

    private fun quote(price: Double) = NetworkQuote(
        symbol = "AAPL", currentPrice = price, change = 1.0, percentChange = 0.5,
        highPrice = price + 2, lowPrice = price - 2, openPrice = price - 1,
        previousClose = price - 1, timestamp = 1000L,
    )

    @Before
    fun setUp() {
        localSource = mockk()
        remoteSource = mockk()
        repository = AlertRepositoryImpl(localSource, remoteSource)
    }

    // ── observeAlerts ────────────────────────────────────────────────────────

    @Test
    fun `observeAlerts maps entities to domain models`() = runTest {
        every { localSource.observeAlerts() } returns flowOf(listOf(entity()))

        repository.observeAlerts().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("AAPL", list[0].symbol)
            cancel()
        }
    }

    // ── createAlert ──────────────────────────────────────────────────────────

    @Test
    fun `createAlert persists entity with enabled true and triggered false`() = runTest {
        coEvery { localSource.insert(any()) } returns 1L

        val result = repository.createAlert("AAPL", AlertCondition.ABOVE, 200.0)

        assertTrue(result is Result.Success)
        coVerify {
            localSource.insert(match { it.isEnabled && !it.isTriggered && it.targetPrice == 200.0 })
        }
    }

    // ── deleteAlert ──────────────────────────────────────────────────────────

    @Test
    fun `deleteAlert delegates to local source`() = runTest {
        coEvery { localSource.deleteById(1L) } returns Unit

        val result = repository.deleteAlert(1L)

        assertTrue(result is Result.Success)
        coVerify { localSource.deleteById(1L) }
    }

    // ── setAlertEnabled ──────────────────────────────────────────────────────

    @Test
    fun `setAlertEnabled delegates to local source`() = runTest {
        coEvery { localSource.setEnabled(1L, false) } returns Unit

        val result = repository.setAlertEnabled(1L, false)

        assertTrue(result is Result.Success)
        coVerify { localSource.setEnabled(1L, false) }
    }

    // ── observeTriggeredAlerts ───────────────────────────────────────────────
    //
    // observeTriggeredAlerts now schedules each active alert on its own
    // independent coroutine (see AlertRepositoryImpl's doc comment), rather
    // than checking the whole set on one shared poll tick. Each test below
    // uses a pollIntervalMillis of 0L so the per-alert loop's initial delay
    // resolves immediately under runTest's virtual clock, then drives that
    // virtual clock forward explicitly to let the launched child coroutine run.

    @Test
    fun `observeTriggeredAlerts emits and marks triggered when ABOVE condition is met`() = runTest {
        val alertEntity = entity(condition = AlertConditionEntity.ABOVE, targetPrice = 200.0)
        every { localSource.observeActiveAlerts() } returns flowOf(listOf(alertEntity))
        coEvery { remoteSource.getQuote("AAPL") } returns quote(price = 205.0)
        coEvery { localSource.markTriggered(1L) } returns Unit

        repository.observeTriggeredAlerts(pollIntervalMillis = 0L).test {
            val triggered = awaitItem()
            assertEquals("AAPL", triggered.symbol)
            assertTrue(triggered.isTriggered)
            cancel()
        }
        coVerify { localSource.markTriggered(1L) }
    }

    @Test
    fun `observeTriggeredAlerts does not emit when condition is not met`() = runTest {
        val alertEntity = entity(condition = AlertConditionEntity.ABOVE, targetPrice = 200.0)
        // A single emission means the per-alert loop checks once, doesn't
        // trigger, then sleeps — it never asks observeActiveAlerts() again
        // within this test's scope, so this flowOf is sufficient.
        every { localSource.observeActiveAlerts() } returns flowOf(listOf(alertEntity))
        coEvery { remoteSource.getQuote("AAPL") } returns quote(price = 195.0)

        repository.observeTriggeredAlerts(pollIntervalMillis = 0L).test {
            expectNoEvents()
            cancel()
        }
        coVerify(exactly = 0) { localSource.markTriggered(any()) }
    }

    @Test
    fun `observeTriggeredAlerts skips an alert whose quote fetch fails without crashing`() = runTest {
        val alertEntity = entity(id = 2L, condition = AlertConditionEntity.BELOW, targetPrice = 100.0)
        every { localSource.observeActiveAlerts() } returns flowOf(listOf(alertEntity))
        coEvery { remoteSource.getQuote("AAPL") } throws RuntimeException("network down")

        repository.observeTriggeredAlerts(pollIntervalMillis = 0L).test {
            expectNoEvents()
            cancel()
        }
        coVerify(exactly = 0) { localSource.markTriggered(any()) }
    }

    @Test
    fun `observeTriggeredAlerts emits for BELOW condition when price drops to target`() = runTest {
        val alertEntity = entity(condition = AlertConditionEntity.BELOW, targetPrice = 150.0)
        every { localSource.observeActiveAlerts() } returns flowOf(listOf(alertEntity))
        coEvery { remoteSource.getQuote("AAPL") } returns quote(price = 145.0)
        coEvery { localSource.markTriggered(1L) } returns Unit

        repository.observeTriggeredAlerts(pollIntervalMillis = 0L).test {
            val triggered = awaitItem()
            assertEquals(AlertCondition.BELOW, triggered.condition)
            cancel()
        }
    }

    @Test
    fun `observeTriggeredAlerts schedules each alert independently and does not duplicate a running loop`() = runTest {
        // Re-emitting the SAME active-alert list a second time (e.g. an
        // unrelated alert's enabled flag changing) must not start a second
        // loop for an alert that's already running — it should fire exactly
        // once when its condition is met, not once per re-emission.
        val alertEntity = entity(condition = AlertConditionEntity.ABOVE, targetPrice = 200.0)
        every { localSource.observeActiveAlerts() } returns flowOf(
            listOf(alertEntity),
            listOf(alertEntity),
        )
        coEvery { remoteSource.getQuote("AAPL") } returns quote(price = 205.0)
        coEvery { localSource.markTriggered(1L) } returns Unit

        repository.observeTriggeredAlerts(pollIntervalMillis = 0L).test {
            awaitItem()
            cancel()
        }
        coVerify(exactly = 1) { localSource.markTriggered(1L) }
    }
}
