package com.nagaraju.stocktracker.core.network

import com.nagaraju.stocktracker.core.network.interceptor.HttpException
import com.nagaraju.stocktracker.core.network.interceptor.NetworkResultInterceptor
import com.nagaraju.stocktracker.core.network.interceptor.RateLimitException
import com.nagaraju.stocktracker.core.network.interceptor.ServerException
import com.nagaraju.stocktracker.core.network.interceptor.UnauthorizedException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

class NetworkResultInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        client = OkHttpClient.Builder()
            .addInterceptor(NetworkResultInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun execute(): Pair<Boolean, IOException?> {
        val request = Request.Builder().url(server.url("/quote")).build()
        return try {
            client.newCall(request).execute().use { it.isSuccessful to null }
        } catch (e: IOException) {
            false to e
        }
    }

    @Test
    fun `200 response passes through without throwing`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder().url(server.url("/quote")).build()
        client.newCall(request).execute().use { response ->
            assertTrue(response.isSuccessful)
        }
    }

    @Test
    fun `401 throws UnauthorizedException`() {
        server.enqueue(MockResponse().setResponseCode(401).setBody("Unauthorized"))

        val (_, exception) = execute()

        assertTrue(exception is UnauthorizedException)
    }

    @Test
    fun `403 throws UnauthorizedException`() {
        server.enqueue(MockResponse().setResponseCode(403).setBody("Forbidden"))

        val (_, exception) = execute()

        assertTrue(exception is UnauthorizedException)
    }

    @Test
    fun `429 throws RateLimitException`() {
        server.enqueue(MockResponse().setResponseCode(429).setBody("Too Many Requests"))

        val (_, exception) = execute()

        assertTrue(exception is RateLimitException)
    }

    @Test
    fun `500 throws ServerException`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        val (_, exception) = execute()

        assertTrue(exception is ServerException)
    }

    @Test
    fun `503 throws ServerException`() {
        server.enqueue(MockResponse().setResponseCode(503).setBody("Service Unavailable"))

        val (_, exception) = execute()

        assertTrue(exception is ServerException)
    }

    @Test
    fun `404 throws generic HttpException with correct code`() {
        server.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val (_, exception) = execute()

        assertTrue(exception is HttpException)
        assertEquals(404, (exception as HttpException).code)
    }

    @Test
    fun `400 throws generic HttpException with correct code`() {
        server.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        val (_, exception) = execute()

        assertTrue(exception is HttpException)
        assertEquals(400, (exception as HttpException).code)
    }
}
