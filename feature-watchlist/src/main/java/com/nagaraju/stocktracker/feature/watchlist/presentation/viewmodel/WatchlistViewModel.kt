package com.nagaraju.stocktracker.feature.watchlist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagaraju.stocktracker.core.common.constants.AppConstants
import com.nagaraju.stocktracker.domain.exception.DomainException
import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.usecase.watchlist.AddToWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.GetWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.RemoveFromWatchlistUseCase
import com.nagaraju.stocktracker.domain.usecase.watchlist.SearchStocksUseCase
import com.nagaraju.stocktracker.feature.watchlist.presentation.state.WatchlistEvent
import com.nagaraju.stocktracker.feature.watchlist.presentation.state.WatchlistUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 350L

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val searchStocksUseCase: SearchStocksUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WatchlistEvent>()
    val events: SharedFlow<WatchlistEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null

    init {
        observeWatchlist()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            getWatchlistUseCase(AppConstants.DEFAULT_POLL_INTERVAL_MS).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update {
                        // Only show the full-screen loading indicator on first load —
                        // subsequent polls should refresh silently in the background.
                        it.copy(isLoading = it.stocks.isEmpty(), errorMessage = null)
                    }

                    is Result.Success -> _uiState.update {
                        it.copy(stocks = result.data, isLoading = false, errorMessage = null)
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        // Keep previously loaded stocks visible — a failed poll
                        // shouldn't blank out data the user can already see.
                        if (_uiState.value.stocks.isEmpty()) {
                            _uiState.update { it.copy(errorMessage = result.exception.toMessage()) }
                        } else {
                            _events.emit(WatchlistEvent.ShowError(result.exception.toMessage()))
                        }
                    }
                }
            }
        }
    }

    fun onAddStockClick() {
        _uiState.update { it.copy(isSearchSheetVisible = true) }
    }

    fun onSearchSheetDismiss() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                isSearchSheetVisible = false,
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            // Debounce so we don't fire a network search request on every keystroke,
            // protecting the Finnhub free-tier rate limit.
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(isSearching = true) }
            when (val result = searchStocksUseCase(query)) {
                is Result.Success -> _uiState.update {
                    it.copy(searchResults = result.data, isSearching = false)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSearching = false) }
                    _events.emit(WatchlistEvent.ShowError(result.exception.toMessage()))
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun onSearchResultClick(symbol: String, companyName: String) {
        viewModelScope.launch {
            when (val result = addToWatchlistUseCase(symbol, companyName)) {
                is Result.Success -> {
                    _events.emit(WatchlistEvent.StockAdded(symbol))
                    onSearchSheetDismiss()
                }
                is Result.Error -> _events.emit(WatchlistEvent.ShowError(result.exception.toMessage()))
                is Result.Loading -> Unit
            }
        }
    }

    fun onRemoveStockClick(symbol: String) {
        viewModelScope.launch {
            when (val result = removeFromWatchlistUseCase(symbol)) {
                is Result.Success -> _events.emit(WatchlistEvent.StockRemoved(symbol))
                is Result.Error -> _events.emit(WatchlistEvent.ShowError(result.exception.toMessage()))
                is Result.Loading -> Unit
            }
        }
    }

    /** Maps a thrown exception to a user-facing message, preferring [DomainException]'s own copy. */
    private fun Throwable.toMessage(): String =
        if (this is DomainException) message ?: "Something went wrong" else "Something went wrong"
}
