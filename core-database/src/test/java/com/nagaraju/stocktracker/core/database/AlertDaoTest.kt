package com.nagaraju.stocktracker.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.nagaraju.stocktracker.core.database.entity.AlertConditionEntity
import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AlertDaoTest {

    private lateinit var database: StockDatabase
    private lateinit var dao: AlertDao

    private fun sampleAlert(
        symbol: String = "AAPL",
        condition: AlertConditionEntity = AlertConditionEntity.ABOVE,
        targetPrice: Double = 200.0,
        isEnabled: Boolean = true,
        isTriggered: Boolean = false,
        createdAt: Long = 1000L,
    ) = AlertEntity(
        symbol      = symbol,
        condition   = condition,
        targetPrice = targetPrice,
        isEnabled   = isEnabled,
        isTriggered = isTriggered,
        createdAt   = createdAt,
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StockDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.alertDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert then observeAll emits the inserted alert`() = runTest {
        dao.insert(sampleAlert())

        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("AAPL", list[0].symbol)
            cancel()
        }
    }

    @Test
    fun `observeAll orders by createdAt descending`() = runTest {
        dao.insert(sampleAlert(symbol = "AAPL", createdAt = 1000L))
        dao.insert(sampleAlert(symbol = "TSLA", createdAt = 3000L))
        dao.insert(sampleAlert(symbol = "MSFT", createdAt = 2000L))

        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(listOf("TSLA", "MSFT", "AAPL"), list.map { it.symbol })
            cancel()
        }
    }

    @Test
    fun `observeActiveAlerts excludes disabled alerts`() = runTest {
        dao.insert(sampleAlert(symbol = "AAPL", isEnabled = true))
        dao.insert(sampleAlert(symbol = "TSLA", isEnabled = false))

        dao.observeActiveAlerts().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("AAPL", list[0].symbol)
            cancel()
        }
    }

    @Test
    fun `observeActiveAlerts excludes already triggered alerts`() = runTest {
        dao.insert(sampleAlert(symbol = "AAPL", isTriggered = false))
        dao.insert(sampleAlert(symbol = "TSLA", isTriggered = true))

        dao.observeActiveAlerts().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("AAPL", list[0].symbol)
            cancel()
        }
    }

    @Test
    fun `setEnabled toggles isEnabled flag without affecting other fields`() = runTest {
        val id = dao.insert(sampleAlert(isEnabled = true))

        dao.setEnabled(id, false)

        dao.observeAll().test {
            val alert = awaitItem().first()
            assertFalse(alert.isEnabled)
            assertEquals(200.0, alert.targetPrice, 0.001)
            cancel()
        }
    }

    @Test
    fun `markTriggered sets isTriggered to true`() = runTest {
        val id = dao.insert(sampleAlert(isTriggered = false))

        dao.markTriggered(id)

        dao.observeAll().test {
            assertTrue(awaitItem().first().isTriggered)
            cancel()
        }
    }

    @Test
    fun `deleteById removes only the matching alert`() = runTest {
        val firstId = dao.insert(sampleAlert(symbol = "AAPL"))
        dao.insert(sampleAlert(symbol = "TSLA"))

        dao.deleteById(firstId)

        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("TSLA", list[0].symbol)
            cancel()
        }
    }

    @Test
    fun `update persists condition and targetPrice changes`() = runTest {
        val id = dao.insert(sampleAlert(condition = AlertConditionEntity.ABOVE, targetPrice = 200.0))

        dao.observeAll().test {
            val original = awaitItem().first()
            dao.update(original.copy(condition = AlertConditionEntity.BELOW, targetPrice = 150.0))

            val updated = awaitItem().first()
            assertEquals(AlertConditionEntity.BELOW, updated.condition)
            assertEquals(150.0, updated.targetPrice, 0.001)
            cancel()
        }
    }
}
