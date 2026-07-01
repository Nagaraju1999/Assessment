package com.nagaraju.stocktracker.domain.result

/**
 * A discriminated union that encapsulates a successful outcome, a loading state,
 * or a failure with a [Throwable] cause.
 *
 * Every repository function and use case returns [Result] so the presentation
 * layer never needs to handle raw exceptions.
 */
sealed class Result<out T> {

    data class Success<out T>(val data: T) : Result<T>()

    data class Error(val exception: Throwable) : Result<Nothing>()

    data object Loading : Result<Nothing>()
}

/** Returns `true` only when this is a [Result.Success] carrying data. */
val Result<*>.isSuccess: Boolean get() = this is Result.Success

/** Returns `true` only when this is a [Result.Error]. */
val Result<*>.isError: Boolean get() = this is Result.Error

/** Returns `true` only when this is [Result.Loading]. */
val Result<*>.isLoading: Boolean get() = this is Result.Loading

/**
 * Returns the encapsulated data if this is [Result.Success], otherwise `null`.
 */
fun <T> Result<T>.getOrNull(): T? = (this as? Result.Success)?.data

/**
 * Returns the encapsulated data if this is [Result.Success], otherwise [default].
 */
fun <T> Result<T>.getOrDefault(default: T): T = getOrNull() ?: default

/**
 * Transforms the data inside [Result.Success] using [transform].
 * [Result.Error] and [Result.Loading] are passed through unchanged.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error   -> this
    is Result.Loading -> Result.Loading
}

/**
 * Invokes [onSuccess], [onError], or [onLoading] depending on the state,
 * then returns this [Result] unchanged (useful for side-effects in a chain).
 */
inline fun <T> Result<T>.onSuccess(onSuccess: (T) -> Unit): Result<T> {
    if (this is Result.Success) onSuccess(data)
    return this
}

inline fun <T> Result<T>.onError(onError: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) onError(exception)
    return this
}

inline fun <T> Result<T>.onLoading(onLoading: () -> Unit): Result<T> {
    if (this is Result.Loading) onLoading()
    return this
}
