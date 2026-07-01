package com.nagaraju.stocktracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nagaraju.stocktracker.core.database.converter.Converters
import com.nagaraju.stocktracker.core.database.dao.AlertDao
import com.nagaraju.stocktracker.core.database.dao.WatchlistDao
import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity

/**
 * Single Room database for the app, holding the watchlist and alerts tables.
 *
 * A single database (rather than one per feature) is intentional: both
 * tables are small, share no complex relations, and splitting them would
 * add multi-database transaction complexity with no real benefit here.
 */
@Database(
    entities = [WatchlistEntity::class, AlertEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class StockDatabase : RoomDatabase() {

    abstract fun watchlistDao(): WatchlistDao

    abstract fun alertDao(): AlertDao

    companion object {
        const val DATABASE_NAME = "stock_tracker.db"
    }
}
