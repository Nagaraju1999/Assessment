package com.nagaraju.stocktracker.core.network

import com.nagaraju.stocktracker.core.network.throttle.ApiRateLimiter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApiRateLimiterTest {

    private lateinit var rateLimiter: ApiRateLimiter
    private var fakeNowMillis = 0L

    @Before
    fun setUp() {
        rateLimiter = ApiRateLimiter()
        rateLimiter.clock = { fakeNowMillis }
    }

    @Test
    fun `tryAcquire succeeds while under the per-window budget`() = runTest {
        repeat(50) {
            assertTrue(rateLimiter.tryAcquire())
        }
    }

    @Test
    fun `tryAcquire fails once the per-window budget is exhausted`() = runTest {
        repeat(50) { rateLimiter.tryAcquire() }

        assertFalse(rateLimiter.tryAcquire())
    }

    @Test
    fun `tryAcquire succeeds again after the window slides past the oldest request`() = runTest {
        repeat(50) { rateLimiter.tryAcquire() }
        assertFalse(rateLimiter.tryAcquire())

        // Advance the fake clock past the 60-second sliding window.
        fakeNowMillis += 60_001L

        assertTrue(rateLimiter.tryAcquire())
    }

    @Test
    fun `requests within the window remain counted against the budget`() = runTest {
        repeat(50) { rateLimiter.tryAcquire() }

        // Advance only partway through the window — old requests should
        // still count, so the limiter should still refuse.
        fakeNowMillis += 30_000L

        assertFalse(rateLimiter.tryAcquire())
    }

    @Test
    fun `eviction is gradual as the window slides one request at a time`() = runTest {
        // Fill the budget with requests spread one millisecond apart.
        repeat(50) {
            rateLimiter.tryAcquire()
            fakeNowMillis += 1L
        }
        assertFalse(rateLimiter.tryAcquire())

        // Advance just past the very first request's window — exactly one
        // slot should free up.
        fakeNowMillis += 60_000L

        assertTrue(rateLimiter.tryAcquire())
        assertFalse(rateLimiter.tryAcquire())
    }
}
