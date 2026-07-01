package com.nagaraju.stocktracker.feature.alerts

import com.nagaraju.stocktracker.feature.alerts.presentation.state.AlertsUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertsUiStateTest {

    @Test
    fun `isAddDialogValid is false when symbol is blank`() {
        val state = AlertsUiState(addDialogSymbol = "", addDialogTargetPrice = "200")
        assertFalse(state.isAddDialogValid)
    }

    @Test
    fun `isAddDialogValid is false when target price is blank`() {
        val state = AlertsUiState(addDialogSymbol = "AAPL", addDialogTargetPrice = "")
        assertFalse(state.isAddDialogValid)
    }

    @Test
    fun `isAddDialogValid is false when target price is zero`() {
        val state = AlertsUiState(addDialogSymbol = "AAPL", addDialogTargetPrice = "0")
        assertFalse(state.isAddDialogValid)
    }

    @Test
    fun `isAddDialogValid is false when target price is not a number`() {
        val state = AlertsUiState(addDialogSymbol = "AAPL", addDialogTargetPrice = "abc")
        assertFalse(state.isAddDialogValid)
    }

    @Test
    fun `isAddDialogValid is true with a valid symbol and positive price`() {
        val state = AlertsUiState(addDialogSymbol = "AAPL", addDialogTargetPrice = "200.50")
        assertTrue(state.isAddDialogValid)
    }

    @Test
    fun `isEmpty is true with no alerts not loading and no error`() {
        val state = AlertsUiState(alerts = emptyList(), isLoading = false, errorMessage = null)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `isEmpty is false while loading`() {
        val state = AlertsUiState(alerts = emptyList(), isLoading = true)
        assertFalse(state.isEmpty)
    }
}
