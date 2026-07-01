package com.nagaraju.stocktracker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    /**
     * Observes all watchlist entries ordered by insertion time (oldest first),
     * so newly added stocks appear at the bottom of the list.
     */
    @Query("SELECT * FROM watchlist ORDER BY addedAt ASC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    /**
     * Observes a single watchlist entry by [symbol], or `null` if not present.
     * Used to check "is this stock already in my watchlist?" reactively.
     */
    @Query("SELECT * FROM watchlist WHERE symbol = :symbol LIMIT 1")
    fun observeBySymbol(symbol: String): Flow<WatchlistEntity?>

    /**
     * Inserts a new watchlist entry. [OnConflictStrategy.REPLACE] makes this
     * idempotent — re-adding an already-watched symbol simply updates it
     * rather than throwing a primary key conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WatchlistEntity)

    @Delete
    suspend fun delete(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun deleteBySymbol(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    suspend fun exists(symbol: String): Boolean

    /**
     * Writes the latest successful quote into the cache columns. Called
     * after every successful poll so the cache is always as fresh as the
     * last good network response, ready to serve if the next poll fails.
     */
    @Query(
        """
        UPDATE watchlist
        SET cachedPrice = :price, cachedChange = :change,
            cachedPercentChange = :percentChange, cachedAt = :cachedAt
        WHERE symbol = :symbol
        """,
    )
    suspend fun updateCachedQuote(
        symbol: String,
        price: Double,
        change: Double,
        percentChange: Double,
        cachedAt: Long,
    )
}
