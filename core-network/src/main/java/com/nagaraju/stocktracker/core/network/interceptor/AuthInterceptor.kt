package com.nagaraju.stocktracker.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Appends the Finnhub API key as a `token` query parameter to every outgoing
 * HTTP request, so individual [FinnhubApi] functions do not need to declare it.
 *
 * The key is provided by Hilt from [BuildConfig.FINNHUB_API_KEY], which is
 * injected into the build from `local.properties` at compile time — never
 * hardcoded in source or committed to version control.
 */
class AuthInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl     = originalRequest.url

        val urlWithToken = originalUrl.newBuilder()
            .addQueryParameter("token", apiKey)
            .build()

        val authenticatedRequest = originalRequest.newBuilder()
            .url(urlWithToken)
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
