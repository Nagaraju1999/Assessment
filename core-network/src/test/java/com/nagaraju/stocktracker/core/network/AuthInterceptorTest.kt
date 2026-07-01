package com.nagaraju.stocktracker.core.network

import com.nagaraju.stocktracker.core.network.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor("test_api_key_123"))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `intercept appends token query parameter to request URL`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/quote?symbol=AAPL"))
            .build()

        client.newCall(request).execute().use { /* drain response */ }

        val recordedRequest = server.takeRequest()
        val tokenParam = recordedRequest.requestUrl?.queryParameter("token")

        assertEquals("test_api_key_123", tokenParam)
    }

    @Test
    fun `intercept preserves existing query parameters`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/quote?symbol=TSLA"))
            .build()

        client.newCall(request).execute().use { /* drain response */ }

        val recordedRequest = server.takeRequest()
        val symbolParam = recordedRequest.requestUrl?.queryParameter("symbol")
        val tokenParam  = recordedRequest.requestUrl?.queryParameter("token")

        assertEquals("TSLA", symbolParam)
        assertNotNull(tokenParam)
    }

    @Test
    fun `intercept does not mutate the request method or body`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder()
            .url(server.url("/search?q=apple"))
            .build()

        client.newCall(request).execute().use { /* drain response */ }

        val recordedRequest = server.takeRequest()
        assertEquals("GET", recordedRequest.method)
    }
}
