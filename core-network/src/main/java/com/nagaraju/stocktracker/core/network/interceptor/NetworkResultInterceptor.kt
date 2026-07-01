package com.nagaraju.stocktracker.core.network.interceptor

import com.nagaraju.stocktracker.core.common.logger.AppLogger
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

private const val TAG = "NetworkResultInterceptor"

/**
 * Converts non-2xx HTTP responses into [IOException] subclasses so that
 * the repository layer receives typed exceptions rather than Retrofit's
 * generic [retrofit2.HttpException].
 *
 * This interceptor runs after [AuthInterceptor] in the OkHttp chain.
 */
class NetworkResultInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.isSuccessful) return response

        val errorBody = runCatching { response.body?.string() }.getOrNull()

        AppLogger.w(TAG, "HTTP ${response.code} for ${chain.request().url} — $errorBody")

        val exception: IOException = when (response.code) {
            401, 403 -> UnauthorizedException("API key is invalid or missing (${response.code})")
            429       -> RateLimitException("Finnhub rate limit exceeded — back off and retry")
            in 500..599 -> ServerException("Finnhub server error ${response.code}")
            else      -> HttpException(response.code, errorBody ?: "Unknown error")
        }

        // Close the response body to avoid resource leaks before throwing.
        response.close()
        throw exception
    }
}

class UnauthorizedException(message: String) : IOException(message)
class RateLimitException(message: String)    : IOException(message)
class ServerException(message: String)       : IOException(message)
class HttpException(val code: Int, message: String) : IOException("HTTP $code: $message")
