package com.nagaraju.stocktracker.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.nagaraju.stocktracker.core.database.entity.WatchlistEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WatchlistDaoTest {

    private lateinit var database: StockDatabase
    private lateinit var dao: WatchlistDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // In-memory database — wiped when the process dies, perfect for tests.
        database = Room.inMemoryDatabaseBuilder(context, StockDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.watchlistDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert then observeAll emits the inserted entity`() = runTest {
        val entity = WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L)

        dao.insert(entity)

        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("AAPL", list[0].symbol)
            cancel()
        }
    }

    @Test
    fun `observeAll orders entries by addedAt ascending`() = runTest {
        dao.insert(WatchlistEntity(symbol = "TSLA", companyName = "Tesla Inc",  addedAt = 2000L))
        dao.insert(WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc",  addedAt = 1000L))
        dao.insert(WatchlistEntity(symbol = "MSFT", companyName = "Microsoft", addedAt = 3000L))

        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(listOf("AAPL", "TSLA", "MSFT"), list.map { it.symbol })
            cancel()
        }
    }

    @Test
    fun `insert with same symbol replaces existing entry`() = runTest {
        dao.insert(WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L))
        dao.insert(WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc Updated", addedAt = 1000L))

        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Apple Inc Updated", list[0].companyName)
            cancel()
        }
    }

    @Test
    fun `delete removes the entity`() = runTest {
        val entity = WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L)
        dao.insert(entity)

        dao.delete(entity)

        dao.observeAll().test {
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `deleteBySymbol removes the matching entity only`() = runTest {
        dao.insert(WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L))
        dao.insert(WatchlistEntity(symbol = "TSLA", companyName = "Tesla Inc", addedAt = 2000L))

        dao.deleteBySymbol("AAPL")

        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("TSLA", list[0].symbol)
            cancel()
        }
    }

    @Test
    fun `exists returns true only for present symbols`() = runTest {
        dao.insert(WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L))

        assertTrue(dao.exists("AAPL"))
        assertFalse(dao.exists("TSLA"))
    }

    @Test
    fun `observeBySymbol emits null when symbol not present`() = runTest {
        dao.observeBySymbol("AAPL").test {
            assertNull(awaitItem())
            cancel()
        }
    }

    @Test
    fun `observeBySymbol emits the entity when present`() = runTest {
        dao.insert(WatchlistEntity(symbol = "AAPL", companyName = "Apple Inc", addedAt = 1000L))

        dao.observeBySymbol("AAPL").test {
            val result = awaitItem()
            assertEquals("Apple Inc", result?.companyName)
            cancel()
        }
    }
}
