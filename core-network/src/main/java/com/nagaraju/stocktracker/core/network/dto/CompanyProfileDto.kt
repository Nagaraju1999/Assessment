package com.nagaraju.stocktracker.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Raw JSON response from Finnhub GET /stock/profile2.
 * Used on the stock details screen to display company metadata.
 *
 * API reference: https://finnhub.io/docs/api/company-profile2
 */
data class CompanyProfileDto(
    @SerializedName("name")             val name: String?,
    @SerializedName("ticker")           val ticker: String?,
    @SerializedName("exchange")         val exchange: String?,
    @SerializedName("finnhubIndustry")  val industry: String?,
    @SerializedName("logo")             val logoUrl: String?,
    @SerializedName("weburl")           val webUrl: String?,
    @SerializedName("marketCapitalization") val marketCap: Double?,
    @SerializedName("shareOutstanding") val sharesOutstanding: Double?,
    @SerializedName("currency")         val currency: String?,
    @SerializedName("country")          val country: String?,
    @SerializedName("ipo")              val ipoDate: String?,
)
