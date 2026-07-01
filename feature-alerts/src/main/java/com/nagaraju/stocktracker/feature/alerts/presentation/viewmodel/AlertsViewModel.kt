package com.nagaraju.stocktracker.feature.alerts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagaraju.stocktracker.core.common.constants.AppConstants
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.usecase.alert.CreateAlertUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.DeleteAlertUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.GetAlertsUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.ObserveTriggeredAlertsUseCase
import com.nagaraju.stocktracker.domain.usecase.alert.ToggleAlertUseCase
import com.nagaraju.stocktracker.feature.alerts.notification.AlertNotifier
import com.nagaraju.stocktracker.feature.alerts.presentation.state.AlertsEvent
import com.nagaraju.stocktracker.feature.alerts.presentation.state.AlertsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val getAlertsUseCase: GetAlertsUseCase,
    private val createAlertUseCase: CreateAlertUseCase,
    private val deleteAlertUseCase: DeleteAlertUseCase,
    private val toggleAlertUseCase: ToggleAlertUseCase,
    private val observeTriggeredAlertsUseCase: ObserveTriggeredAlertsUseCase,
    private val alertNotifier: AlertNotifier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AlertsEvent>()
    val events: SharedFlow<AlertsEvent> = _events.asSharedFlow()

    init {
        observeAlerts()
        observeTriggeredAlerts()
    }

    private fun observeAlerts() {
        getAlertsUseCase()
            .onEach { alerts -> _uiState.update { it.copy(alerts = alerts, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    /**
     * Continuously evaluates active alerts against live prices. Each
     * trigger both posts a system notification (so the user is informed
     * even if the app is backgrounded) and emits an [AlertsEvent.AlertTriggered]
     * for an in-app Snackbar when the screen is in the foreground.
     */
    private fun observeTriggeredAlerts() {
        observeTriggeredAlertsUseCase(AppConstants.DEFAULT_POLL_INTERVAL_MS)
            .onEach { alert ->
                alertNotifier.notifyTriggered(alert)
                _events.emit(AlertsEvent.AlertTriggered(alert))
            }
            .launchIn(viewModelScope)
    }

    fun onAddAlertClick() {
        _uiState.update { it.copy(isAddDialogVisible = true) }
    }

    fun onAddDialogDismiss() {
        _uiState.update {
            it.copy(
                isAddDialogVisible = false,
                addDialogSymbol = "",
                addDialogCondition = AlertCondition.ABOVE,
                addDialogTargetPrice = "",
            )
        }
    }

    fun onAddDialogSymbolChange(symbol: String) {
        _uiState.update { it.copy(addDialogSymbol = symbol) }
    }

    fun onAddDialogConditionChange(condition: AlertCondition) {
        _uiState.update { it.copy(addDialogCondition = condition) }
    }

    fun onAddDialogTargetPriceChange(price: String) {
        // Only accept digits and a single decimal point, mirroring standard
        // numeric input field behavior without a custom VisualTransformation.
        if (price.isEmpty() || price.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(addDialogTargetPrice = price) }
        }
    }

    fun onAddDialogConfirm() {
        val state = _uiState.value
        if (!state.isAddDialogValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAlert = true) }
            val targetPrice = state.addDialogTargetPrice.toDoubleOrNull() ?: 0.0
            when (
                val result = createAlertUseCase(state.addDialogSymbol, state.addDialogCondition, targetPrice)
            ) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSubmittingAlert = false) }
                    _events.emit(AlertsEvent.AlertCreated(state.addDialogSymbol))
                    onAddDialogDismiss()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSubmittingAlert = false) }
                    _events.emit(AlertsEvent.ShowError(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onDeleteAlertClick(alertId: Long, symbol: String) {
        viewModelScope.launch {
            when (val result = deleteAlertUseCase(alertId)) {
                is Result.Success -> _events.emit(AlertsEvent.AlertDeleted(symbol))
                is Result.Error -> _events.emit(AlertsEvent.ShowError(result.exception.toMessage()))
                is Result.Loading -> Unit
            }
        }
    }

    fun onAlertEnabledToggle(alertId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            when (val result = toggleAlertUseCase(alertId, isEnabled)) {
                is Result.Error -> _events.emit(AlertsEvent.ShowError(result.exception.toMessage()))
                is Result.Success, is Result.Loading -> Unit
            }
        }
    }

    private fun Throwable.toMessage(): String =
        if (this is DomainException) message ?: "Something went wrong" else "Something went wrong"
}
