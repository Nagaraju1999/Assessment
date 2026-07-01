package com.nagaraju.stocktracker.core.common.constants

/**
 * Cross-cutting constants shared by more than one module.
 *
 * Values used in only a single file (e.g. a screen's own debounce window)
 * stay as a private `const val` in that file — this object exists strictly
 * for values that would otherwise be copy-pasted across module boundaries,
 * such as the default polling interval, which independently appeared in
 * [core-common], [domain], and all three feature ViewModels before being
 * consolidated here.
 */
object AppConstants {

    /**
     * Default interval, in milliseconds, between price polls for the
     * watchlist, a single stock's details screen, and alert evaluation.
     *
     * 60 seconds aligns cleanly with Finnhub's free-tier limit of
     * 60 requests/minute — with N stocks in the watchlist, polling every
     * 60 seconds consumes exactly N requests/minute, leaving the remaining
     * budget for user-initiated actions (search, add, navigate to details).
     * 30 seconds was too aggressive when multiple screens were simultaneously
     * active, causing burst rate-limit errors.
     */
    const val DEFAULT_POLL_INTERVAL_MS = 60_000L
}
