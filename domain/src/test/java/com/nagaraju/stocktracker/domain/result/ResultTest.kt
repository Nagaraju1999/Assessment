package com.nagaraju.stocktracker.domain.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    // ── isSuccess / isError / isLoading ──────────────────────────────────────

    @Test
    fun `Success is flagged correctly`() {
        val result: Result<Int> = Result.Success(42)
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertFalse(result.isLoading)
    }

    @Test
    fun `Error is flagged correctly`() {
        val result: Result<Int> = Result.Error(RuntimeException("boom"))
        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertFalse(result.isLoading)
    }

    @Test
    fun `Loading is flagged correctly`() {
        val result: Result<Int> = Result.Loading
        assertFalse(result.isSuccess)
        assertFalse(result.isError)
        assertTrue(result.isLoading)
    }

    // ── getOrNull ─────────────────────────────────────────────────────────────

    @Test
    fun `getOrNull returns data for Success`() {
        assertEquals(42, Result.Success(42).getOrNull())
    }

    @Test
    fun `getOrNull returns null for Error`() {
        assertNull(Result.Error(RuntimeException()).getOrNull())
    }

    @Test
    fun `getOrNull returns null for Loading`() {
        assertNull(Result.Loading.getOrNull())
    }

    // ── getOrDefault ──────────────────────────────────────────────────────────

    @Test
    fun `getOrDefault returns data when Success`() {
        assertEquals(42, Result.Success(42).getOrDefault(0))
    }

    @Test
    fun `getOrDefault returns fallback when Error`() {
        assertEquals(0, Result.Error(RuntimeException()).getOrDefault(0))
    }

    // ── map ───────────────────────────────────────────────────────────────────

    @Test
    fun `map transforms Success data`() {
        val result = Result.Success(10).map { it * 2 }
        assertEquals(Result.Success(20), result)
    }

    @Test
    fun `map passes Error through unchanged`() {
        val error = RuntimeException("fail")
        val result = Result.Error(error).map { (it as Int) * 2 }
        assertEquals(Result.Error(error), result)
    }

    @Test
    fun `map passes Loading through unchanged`() {
        val result = Result.Loading.map { (it as Int) * 2 }
        assertEquals(Result.Loading, result)
    }

    // ── onSuccess / onError callback ──────────────────────────────────────────

    @Test
    fun `onSuccess callback fires for Success only`() {
        var called = false
        Result.Success(1).onSuccess { called = true }
        assertTrue(called)

        called = false
        Result.Error(RuntimeException()).onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun `onError callback fires for Error only`() {
        var called = false
        Result.Error(RuntimeException()).onError { called = true }
        assertTrue(called)

        called = false
        Result.Success(1).onError { called = true }
        assertFalse(called)
    }
}
