package com.nagaraju.stocktracker.feature.alerts

import app.cash.turbine.test
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.usecase.alert.CreateAlertUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.DeleteAlertUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.GetAlertsUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.ObserveTriggeredAlertsUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.ToggleAlertUseCase
import com.nagaraju.stocktracker.feature.alerts.notification.AlertNotifier
import com.nagaraju.stocktracker.feature.alerts.presentation.state.AlertsEvent
import com.nagaraju.stocktracker.feature.alerts.presentation.viewmodel.AlertsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    private lateinit var getAlertsUseCase: GetAlertsUseCase
    private lateinit var createAlertUseCase: CreateAlertUseCase
    private lateinit var deleteAlertUseCase: DeleteAlertUseCase
    private lateinit var toggleAlertUseCase: ToggleAlertUseCase
    private lateinit var observeTriggeredAlertsUseCase: ObserveTriggeredAlertsUseCase
    private lateinit var alertNotifier: AlertNotifier

    private fun alert(
        id: Long = 1L,
        symbol: String = "AAPL",
        condition: AlertCondition = AlertCondition.ABOVE,
        targetPrice: Double = 200.0,
        isTriggered: Boolean = false,
    ) = StockAlert(
        id = id, symbol = symbol, condition = condition, targetPrice = targetPrice,
        isEnabled = true, isTriggered = isTriggered, createdAt = 1000L,
    )

    private fun createViewModel() = AlertsViewModel(
        getAlertsUseCase, createAlertUseCase, deleteAlertUseCase,
        toggleAlertUseCase, observeTriggeredAlertsUseCase, alertNotifier,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        getAlertsUseCase = mockk()
        createAlertUseCase = mockk()
        deleteAlertUseCase = mockk()
        toggleAlertUseCase = mockk()
        observeTriggeredAlertsUseCase = mockk()
        alertNotifier = mockk()

        every { getAlertsUseCase() } returns flowOf(emptyList())
        every { observeTriggeredAlertsUseCase(any()) } returns emptyFlow()
        every { alertNotifier.notifyTriggered(any()) } just runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeAlerts populates state and clears loading`() = runTest {
        every { getAlertsUseCase() } returns flowOf(listOf(alert()))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.alerts.size)
        assertTrue(!viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onAddAlertClick shows the dialog`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddAlertClick()

        assertTrue(viewModel.uiState.value.isAddDialogVisible)
    }

    @Test
    fun `onAddDialogDismiss resets the form fields`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddAlertClick()
        viewModel.onAddDialogSymbolChange("AAPL")
        viewModel.onAddDialogTargetPriceChange("200")

        viewModel.onAddDialogDismiss()

        val state = viewModel.uiState.value
        assertTrue(!state.isAddDialogVisible)
        assertEquals("", state.addDialogSymbol)
        assertEquals("", state.addDialogTargetPrice)
    }

    @Test
    fun `onAddDialogTargetPriceChange rejects non-numeric input`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddDialogTargetPriceChange("abc")

        assertEquals("", viewModel.uiState.value.addDialogTargetPrice)
    }

    @Test
    fun `onAddDialogTargetPriceChange accepts valid decimal input`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddDialogTargetPriceChange("123.45")

        assertEquals("123.45", viewModel.uiState.value.addDialogTargetPrice)
    }

    @Test
    fun `onAddDialogConfirm does nothing when form is invalid`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAddDialogSymbolChange("") // blank — invalid

        viewModel.onAddDialogConfirm()
        advanceUntilIdle()

        coVerify(exactly = 0) { createAlertUseCase(any(), any(), any()) }
    }

    @Test
    fun `onAddDialogConfirm creates alert and dismisses dialog on success`() = runTest {
        coEvery { createAlertUseCase("AAPL", AlertCondition.ABOVE, 200.0) } returns Result.Success(1L)
        val viewModel = createViewModel()
        viewModel.onAddAlertClick()
        viewModel.onAddDialogSymbolChange("AAPL")
        viewModel.onAddDialogTargetPriceChange("200")

        viewModel.events.test {
            viewModel.onAddDialogConfirm()
            val event = awaitItem()
            assertTrue(event is AlertsEvent.AlertCreated)
        }
        advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.isAddDialogVisible)
    }

    @Test
    fun `onAddDialogConfirm emits ShowError and keeps dialog open on failure`() = runTest {
        coEvery { createAlertUseCase("AAPL", AlertCondition.ABOVE, 200.0) } returns
            Result.Error(DomainException.EmptyResponseException("AAPL"))
        val viewModel = createViewModel()
        viewModel.onAddAlertClick()
        viewModel.onAddDialogSymbolChange("AAPL")
        viewModel.onAddDialogTargetPriceChange("200")

        viewModel.events.test {
            viewModel.onAddDialogConfirm()
            val event = awaitItem()
            assertTrue(event is AlertsEvent.ShowError)
        }
        assertTrue(viewModel.uiState.value.isAddDialogVisible)
    }

    @Test
    fun `onDeleteAlertClick emits AlertDeleted on success`() = runTest {
        coEvery { deleteAlertUseCase(1L) } returns Result.Success(Unit)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onDeleteAlertClick(1L, "AAPL")
            val event = awaitItem()
            assertTrue(event is AlertsEvent.AlertDeleted)
        }
    }

    @Test
    fun `onAlertEnabledToggle emits ShowError on failure`() = runTest {
        coEvery { toggleAlertUseCase(1L, false) } returns Result.Error(DomainException.UnknownException())
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAlertEnabledToggle(1L, false)
            val event = awaitItem()
            assertTrue(event is AlertsEvent.ShowError)
        }
    }

    @Test
    fun `triggered alert posts a system notification and emits AlertTriggered event`() = runTest {
        every { observeTriggeredAlertsUseCase(any()) } returns flowOf(alert(isTriggered = true))

        val viewModel = createViewModel()

        viewModel.events.test {
            val event = awaitItem()
            assertTrue(event is AlertsEvent.AlertTriggered)
        }
        verify { alertNotifier.notifyTriggered(any()) }
    }
}
