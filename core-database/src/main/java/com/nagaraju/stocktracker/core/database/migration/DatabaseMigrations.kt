package com.nagaraju.stocktracker.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from schema v1 to v2: adds nullable cached-price columns to
 * the `watchlist` table, supporting the offline-fallback strategy where
 * the last successfully fetched quote is shown (clearly marked as stale)
 * when a live poll fails with no network connection.
 *
 * All four new columns are nullable with no default, matching the Kotlin
 * entity's `= null` defaults — existing rows simply have no cached price
 * until their next successful poll.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE watchlist ADD COLUMN cachedPrice REAL")
        db.execSQL("ALTER TABLE watchlist ADD COLUMN cachedChange REAL")
        db.execSQL("ALTER TABLE watchlist ADD COLUMN cachedPercentChange REAL")
        db.execSQL("ALTER TABLE watchlist ADD COLUMN cachedAt INTEGER")
    }
}
