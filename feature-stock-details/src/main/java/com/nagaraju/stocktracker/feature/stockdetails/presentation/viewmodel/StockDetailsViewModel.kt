package com.nagaraju.stocktracker.feature.stockdetails.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagaraju.stocktracker.core.common.constants.AppConstants
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.model.PriceTrendCalculator
import com.nagaraju.stocktracker.domain.model.TimeRange
import com.nagaraju.stocktracker.domain.repository.StockRepository
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.usecase.stock.GetCompanyProfileUseCase
import com.nagaraju.stocktracker.domain.usecase.stock.GetStockDetailsUseCase
import com.nagaraju.stocktracker.domain.usecase.stock.GetStockHistoryUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.AddToWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.RemoveFromWatchlistUseCase
import com.nagaraju.stocktracker.feature.stockdetails.navigation.StockDetailsRoutes
import com.nagaraju.stocktracker.feature.stockdetails.presentation.state.StockDetailsEvent
import com.nagaraju.stocktracker.feature.stockdetails.presentation.state.StockDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

// Small stagger between sequential API calls on screen open so they don't
// all fire simultaneously and burst past Finnhub's free-tier rate limit.
private const val INIT_STAGGER_MS = 500L

@HiltViewModel
class StockDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStockDetailsUseCase: GetStockDetailsUseCase,
    private val getStockHistoryUseCase: GetStockHistoryUseCase,
    private val getCompanyProfileUseCase: GetCompanyProfileUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val stockRepository: StockRepository,
) : ViewModel() {

    private val symbol: String = checkNotNull(savedStateHandle[StockDetailsRoutes.SYMBOL_ARG]) {
        "StockDetailsViewModel requires a non-null symbol navigation argument"
    }

    private val _uiState = MutableStateFlow(StockDetailsUiState(symbol = symbol))
    val uiState: StateFlow<StockDetailsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StockDetailsEvent>()
    val events: SharedFlow<StockDetailsEvent> = _events.asSharedFlow()

    private var historyJob: Job? = null

    init {
        observeWatchlistStatus()
        viewModelScope.launch {
            // Stagger the three API calls so they don't all fire at the same
            // millisecond on screen open, which would burst past Finnhub's
            // free-tier rate limit when the watchlist is also polling.
            observeQuote()
            delay(INIT_STAGGER_MS)
            loadHistory(TimeRange.ONE_DAY)
            delay(INIT_STAGGER_MS)
            loadCompanyProfile()
        }
    }

    private fun observeQuote() {
        getStockDetailsUseCase(symbol, AppConstants.DEFAULT_POLL_INTERVAL_MS)
            .onEach { result ->
                when (result) {
                    is Result.Loading -> _uiState.update {
                        it.copy(isQuoteLoading = it.stock == null, quoteErrorMessage = null)
                    }
                    is Result.Success -> _uiState.update {
                        it.copy(stock = result.data, isQuoteLoading = false, quoteErrorMessage = null)
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isQuoteLoading = false) }
                        if (_uiState.value.stock == null) {
                            _uiState.update { it.copy(quoteErrorMessage = result.exception.toMessage()) }
                        } else {
                            _events.emit(StockDetailsEvent.ShowError(result.exception.toMessage()))
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeWatchlistStatus() {
        stockRepository.isInWatchlist(symbol)
            .onEach { isInWatchlist -> _uiState.update { it.copy(isInWatchlist = isInWatchlist) } }
            .launchIn(viewModelScope)
    }

    fun onRangeSelected(range: TimeRange) {
        if (range == _uiState.value.selectedRange) return
        _uiState.update { it.copy(selectedRange = range) }
        loadHistory(range)
    }

    private fun loadHistory(range: TimeRange) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            _uiState.update { it.copy(isHistoryLoading = true, historyErrorMessage = null) }
            when (val result = getStockHistoryUseCase(symbol, range)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        history = result.data,
                        isHistoryLoading = false,
                        priceTrend = PriceTrendCalculator.calculate(result.data.candles),
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(isHistoryLoading = false, historyErrorMessage = result.exception.toMessage(), priceTrend = null)
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadCompanyProfile() {
        viewModelScope.launch {
            when (val result = getCompanyProfileUseCase(symbol)) {
                is Result.Success -> _uiState.update { it.copy(companyProfile = result.data) }
                // Profile is supplementary (logo, industry) — a failure here
                // is silently swallowed rather than blocking the rest of the
                // screen, which already has price and chart data.
                is Result.Error, is Result.Loading -> Unit
            }
        }
    }

    fun onWatchlistToggleClick() {
        viewModelScope.launch {
            val companyName = _uiState.value.companyProfile?.name
                ?: _uiState.value.stock?.companyName
                ?: symbol

            if (_uiState.value.isInWatchlist) {
                when (val result = removeFromWatchlistUseCase(symbol)) {
                    is Result.Success -> _events.emit(StockDetailsEvent.RemovedFromWatchlist(symbol))
                    is Result.Error -> _events.emit(StockDetailsEvent.ShowError(result.exception.toMessage()))
                    is Result.Loading -> Unit
                }
            } else {
                when (val result = addToWatchlistUseCase(symbol, companyName)) {
                    is Result.Success -> _events.emit(StockDetailsEvent.AddedToWatchlist(symbol))
                    is Result.Error -> _events.emit(StockDetailsEvent.ShowError(result.exception.toMessage()))
                    is Result.Loading -> Unit
                }
            }
        }
    }

    fun onRetryHistoryClick() {
        loadHistory(_uiState.value.selectedRange)
    }

    private fun Throwable.toMessage(): String =
        if (this is DomainException) message ?: "Something went wrong" else "Something went wrong"
}
