package com.nagaraju.stocktracker.domain

/**
 * Default polling interval, in milliseconds, shared across repository
 * interfaces and use cases in this module.
 *
 * This intentionally duplicates the same value as
 * [com.nagaraju.stocktracker.core.common.constants.AppConstants.DEFAULT_POLL_INTERVAL_MS]
 * rather than referencing it directly — [domain] is a pure Kotlin JVM
 * module with zero Android dependency by design, and `core-common` is an
 * Android library, so a dependency in that direction isn't possible (the
 * same constraint that placed [com.nagaraju.stocktracker.domain.result.Result]
 * in this module instead of `core-common`). If the polling cadence ever
 * changes, both constants must be updated together.
 */
internal const val DEFAULT_POLL_INTERVAL_MS = 60_000L
