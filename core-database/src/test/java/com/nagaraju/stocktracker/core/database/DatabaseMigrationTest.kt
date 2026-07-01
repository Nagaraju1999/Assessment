package com.nagaraju.stocktracker.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.nagaraju.stocktracker.core.database.migration.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseMigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        StockDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun `migrate 1 to 2 preserves existing watchlist rows and adds nullable cache columns`() {
        // Create the database at v1 and insert a row using raw SQL,
        // mirroring what a real pre-migration installed app would have on disk.
        var db = migrationTestHelper.createDatabase(testDbName, 1)
        db.execSQL(
            "INSERT INTO watchlist (symbol, companyName, addedAt) VALUES ('AAPL', 'Apple Inc', 1000)",
        )
        db.close()

        // Run the migration and verify the row survived with the new columns present and null.
        db = migrationTestHelper.runMigrationsAndValidate(testDbName, 2, true, MIGRATION_1_2)

        val cursor = db.query("SELECT symbol, companyName, cachedPrice, cachedAt FROM watchlist")
        cursor.use {
            assertTrue(it.moveToFirst())
            assertEquals("AAPL", it.getString(it.getColumnIndexOrThrow("symbol")))
            assertEquals("Apple Inc", it.getString(it.getColumnIndexOrThrow("companyName")))
            assertTrue(it.isNull(it.getColumnIndexOrThrow("cachedPrice")))
            assertTrue(it.isNull(it.getColumnIndexOrThrow("cachedAt")))
        }
        db.close()
    }
}
