package com.nagaraju.stocktracker.core.network.throttle

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A simple sliding-window rate limiter enforcing a hard ceiling on
 * outgoing Finnhub requests, shared across every caller in the app
 * (watchlist polling, stock-details polling, and alert evaluation).
 *
 * Finnhub's free tier allows 60 requests/minute. [maxRequestsPerWindow]
 * is kept below that (not equal to it) to leave headroom for the
 * occasional ad-hoc call — searching for a symbol, adding to the
 * watchlist — that happens outside the regular polling cadence.
 *
 * This is a safety ceiling, not the primary throttling mechanism — the
 * primary mechanism is [com.nagaraju.stocktracker.domain.model.SmartPollingCalculator],
 * which keeps actual request volume well under this limit in normal use.
 * [ApiRateLimiter] exists to guarantee the limit is never exceeded even in
 * a pathological case, such as many alerts all sitting near their target
 * price simultaneously.
 */
@Singleton
class ApiRateLimiter @Inject constructor() {

    /**
     * Current time in milliseconds. `internal` and `var` solely so unit
     * tests can substitute a controllable clock to verify sliding-window
     * eviction deterministically; production code never reassigns this.
     */
    internal var clock: () -> Long = { System.currentTimeMillis() }

    private val maxRequestsPerWindow = 50
    private val windowMillis = 60_000L

    private val mutex = Mutex()
    private val requestTimestamps = ArrayDeque<Long>()

    /**
     * Returns `true` if a request is allowed right now under the current
     * window, and records it as consumed. Returns `false` if the caller
     * should skip this request — callers treat a `false` result the same
     * way they treat a transient network failure: skip this tick, try
     * again next poll.
     */
    suspend fun tryAcquire(): Boolean = mutex.withLock {
        val now = clock()
        while (requestTimestamps.isNotEmpty() && now - requestTimestamps.first() > windowMillis) {
            requestTimestamps.removeFirst()
        }

        if (requestTimestamps.size >= maxRequestsPerWindow) {
            false
        } else {
            requestTimestamps.addLast(now)
            true
        }
    }
}
