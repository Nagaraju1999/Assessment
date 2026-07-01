package com.nagaraju.stocktracker.core.common.extensions

import com.nagaraju.stocktracker.core.common.constants.AppConstants
import com.nagaraju.stocktracker.domain.result.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Wraps every emission of this [Flow] in [Result.Success] and prepends
 * a [Result.Loading] emission so collectors can display a loading state
 * immediately while data is fetching.
 *
 * Any exception thrown upstream is caught and emitted as [Result.Error]
 * rather than crashing the collector.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it)) }

/**
 * Emits a network/db call result every [intervalMillis] milliseconds,
 * starting immediately. The [block] lambda is re-invoked on each tick.
 *
 * Used by repositories to implement periodic price polling without
 * WorkManager overhead while the app is in the foreground.
 *
 * @param intervalMillis Milliseconds between successive calls (default 30 s).
 * @param block          Suspend function that performs the data fetch.
 */
fun <T> pollingFlow(
    intervalMillis: Long = AppConstants.DEFAULT_POLL_INTERVAL_MS,
    block: suspend () -> T,
): Flow<T> = flow {
    while (true) {
        emit(block())
        delay(intervalMillis)
    }
}
