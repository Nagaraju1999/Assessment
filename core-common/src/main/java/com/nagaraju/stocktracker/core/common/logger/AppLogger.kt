package com.nagaraju.stocktracker.core.common.logger

import android.util.Log

/**
 * Centralises all logging so the strategy (Logcat, Timber, Firebase Crashlytics)
 * can be swapped in one place without touching call sites.
 *
 * Call sites pass a [tag] explicitly rather than using `javaClass.simpleName`
 * to keep log filtering predictable.
 */
object AppLogger {

    private const val GLOBAL_TAG = "StockTracker"

    fun d(tag: String = GLOBAL_TAG, message: String) {
        Log.d(tag, message)
    }

    fun i(tag: String = GLOBAL_TAG, message: String) {
        Log.i(tag, message)
    }

    fun w(tag: String = GLOBAL_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
    }

    fun e(tag: String = GLOBAL_TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
    }
}
