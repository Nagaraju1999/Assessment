package com.nagaraju.stocktracker.domain.exception

/**
 * Sealed hierarchy of errors the domain layer can throw.
 *
 * Data-layer exceptions (network IOExceptions, Room exceptions) are caught
 * by repository implementations and re-thrown as one of these, so use cases
 * and the presentation layer only ever need to handle this closed set.
 */
sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** No network connection was available when a network-dependent call was made. */
    class NoInternetException(cause: Throwable? = null) :
        DomainException("No internet connection. Showing cached data if available.", cause)

    /** The Finnhub API key is missing or invalid. */
    class UnauthorizedException(cause: Throwable? = null) :
        DomainException("API authentication failed. Check your API key configuration.", cause)

    /** The Finnhub free-tier rate limit was exceeded. */
    class RateLimitExceededException(cause: Throwable? = null) :
        DomainException("Too many requests. Please wait a moment and try again.", cause)

    /** The remote server returned a 5xx error. */
    class ServerException(cause: Throwable? = null) :
        DomainException("The server is currently unavailable. Please try again later.", cause)

    /** The request timed out before receiving a response. */
    class TimeoutException(cause: Throwable? = null) :
        DomainException("The request timed out. Please check your connection.", cause)

    /** The API returned a successful response with no usable data (e.g. unknown symbol). */
    class EmptyResponseException(symbol: String? = null) :
        DomainException(
            if (symbol != null) "No data available for \"$symbol\"." else "No data available."
        )

    /** A symbol the user searched for or tried to add does not exist on the watchlist. */
    class StockNotFoundException(symbol: String) :
        DomainException("Stock \"$symbol\" was not found.")

    /** Catch-all for anything that doesn't map to a more specific case above. */
    class UnknownException(cause: Throwable? = null) :
        DomainException(cause?.message ?: "An unexpected error occurred.", cause)
}
