package com.nagaraju.stocktracker.feature.stockdetails

import com.nagaraju.stocktracker.feature.stockdetails.navigation.StockDetailsRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StockDetailsRoutesTest {

    @Test
    fun `createRoute builds the expected path for a simple symbol`() {
        assertEquals("stock_details/AAPL", StockDetailsRoutes.createRoute("AAPL"))
    }

    @Test
    fun `createRoute is consistent with the registered route pattern argument name`() {
        // The pattern declares {symbol} — createRoute must substitute exactly
        // that segment, or NavGraphBuilder.composable() would never match
        // a route built by createRoute() at runtime.
        assertTrue(StockDetailsRoutes.STOCK_DETAILS_PATTERN.contains("{${StockDetailsRoutes.SYMBOL_ARG}}"))
    }

    @Test
    fun `deepLinks returns exactly one NavDeepLink`() {
        assertEquals(1, StockDetailsRoutes.deepLinks().size)
    }
}
