package com.nagaraju.stocktracker.core.network.api

import com.nagaraju.stocktracker.core.network.dto.CandleDto
import com.nagaraju.stocktracker.core.network.dto.CompanyProfileDto
import com.nagaraju.stocktracker.core.network.dto.QuoteDto
import com.nagaraju.stocktracker.core.network.dto.SearchResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the Finnhub REST API.
 *
 * The API key is injected by [AuthInterceptor] as a query parameter on every
 * request, so no [Query] annotation for the token appears here — keeping
 * all endpoint declarations free of authentication boilerplate.
 *
 * All functions are `suspend` so Retrofit executes them on the calling
 * coroutine's dispatcher (typically [Dispatchers.IO]).
 */
interface FinnhubApi {

    /**
     * Fetches the real-time quote for a given [symbol].
     * Finnhub free tier: 60 API calls/minute.
     *
     * @param symbol Exchange symbol. e.g. "AAPL"
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
    ): QuoteDto

    /**
     * Fetches OHLCV candle data for [symbol] over a time range.
     *
     * @param symbol     Exchange symbol. e.g. "AAPL"
     * @param resolution Candle resolution: "1","5","15","30","60","D","W","M"
     * @param from       Start of range as Unix epoch seconds.
     * @param to         End of range as Unix epoch seconds.
     */
    @GET("stock/candle")
    suspend fun getCandles(
        @Query("symbol")     symbol: String,
        @Query("resolution") resolution: String,
        @Query("from")       from: Long,
        @Query("to")         to: Long,
    ): CandleDto

    /**
     * Searches for symbols matching [query].
     *
     * @param query Free-text search term. e.g. "apple"
     */
    @GET("search")
    suspend fun searchSymbol(
        @Query("q") query: String,
    ): SearchResultDto

    /**
     * Fetches company profile metadata for [symbol].
     *
     * @param symbol Exchange symbol. e.g. "AAPL"
     */
    @GET("stock/profile2")
    suspend fun getCompanyProfile(
        @Query("symbol") symbol: String,
    ): CompanyProfileDto
}
