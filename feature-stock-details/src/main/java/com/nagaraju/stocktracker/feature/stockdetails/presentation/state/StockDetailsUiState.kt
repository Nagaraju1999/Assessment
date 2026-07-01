package com.nagaraju.stocktracker.feature.stockdetails.presentation.state

import com.nagaraju.stocktracker.domain.model.CompanyProfile
import com.nagaraju.stocktracker.domain.model.PriceTrend
import com.nagaraju.stocktracker.domain.model.Stock
import com.nagaraju.stocktracker.domain.model.StockHistory
import com.nagaraju.stocktracker.domain.model.TimeRange

/**
 * Immutable snapshot for the stock details screen.
 *
 * Quote, history, and profile are tracked as three independent loading
 * states (rather than one combined flag) because they load and can fail
 * independently: a chart-data failure shouldn't block the live price
 * header from rendering, and vice versa.
 */
data class StockDetailsUiState(
    val symbol: String = "",
    val stock: Stock? = null,
    val isQuoteLoading: Boolean = true,
    val quoteErrorMessage: String? = null,
    val selectedRange: TimeRange = TimeRange.ONE_DAY,
    val history: StockHistory? = null,
    val isHistoryLoading: Boolean = true,
    val historyErrorMessage: String? = null,
    val companyProfile: CompanyProfile? = null,
    val isInWatchlist: Boolean = false,
    /**
     * Derived from [history] via [com.nagaraju.stocktracker.domain.model.PriceTrendCalculator]
     * whenever new history loads — not fetched independently, since the
     * trend is purely a function of the candle data already on screen.
     * `null` while history is loading, on error, or when there isn't
     * enough data for a meaningful trend line.
     */
    val priceTrend: PriceTrend? = null,
) {
    val isChartEmpty: Boolean get() =
        history?.candles?.isEmpty() == true && !isHistoryLoading && historyErrorMessage == null
}
