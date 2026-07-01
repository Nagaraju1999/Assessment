package com.nagaraju.stocktracker.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Abstracts [CoroutineDispatcher] selection so that unit tests can inject
 * [TestCoroutineDispatcher] without touching production code.
 *
 * Inject this interface wherever a dispatcher is needed instead of
 * referencing [Dispatchers] directly.
 */
interface DispatcherProvider {
    /** CPU-bound work: sorting, filtering, mapping. */
    val main: CoroutineDispatcher

    /** UI thread — collecting StateFlow, updating UI state. */
    val io: CoroutineDispatcher

    /** Disk / network I/O: Room queries, Retrofit calls. */
    val default: CoroutineDispatcher
}

/**
 * Production implementation backed by the standard [Dispatchers] singletons.
 * Hilt binds this in [core-common]'s DI module as the default [DispatcherProvider].
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher    = Dispatchers.Main
    override val io: CoroutineDispatcher      = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
