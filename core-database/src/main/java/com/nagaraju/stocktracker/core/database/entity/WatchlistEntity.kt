package com.nagaraju.stocktracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted record of a stock the user has added to their watchlist.
 *
 * Identity fields ([symbol], [companyName], [addedAt]) are the source of
 * truth. The cached price fields ([cachedPrice] et al.) are a best-effort
 * snapshot of the last successful quote, written on every successful poll
 * and read only as an offline fallback when a poll fails with no network
 * connection — see [com.nagaraju.stocktracker.data.repository.StockRepositoryImpl]'s
 * offline strategy. They are never treated as authoritative; a live quote
 * always overwrites them.
 *
 * @param symbol         Exchange symbol, primary key. e.g. "AAPL"
 * @param companyName    Display name shown in the watchlist row.
 * @param addedAt        Unix epoch seconds when the stock was added.
 * @param cachedPrice    Last successfully fetched current price, or `null`
 *                        if no successful poll has happened yet.
 * @param cachedChange   Last successfully fetched absolute price change.
 * @param cachedPercentChange Last successfully fetched percent change.
 * @param cachedAt       Unix epoch seconds when the cached fields were last
 *                        updated — surfaced in the UI as "as of HH:mm" when
 *                        showing offline data, so the user knows it's stale.
 */
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val symbol: String,
    val companyName: String,
    val addedAt: Long,
    val cachedPrice: Double? = null,
    val cachedChange: Double? = null,
    val cachedPercentChange: Double? = null,
    val cachedAt: Long? = null,
)
