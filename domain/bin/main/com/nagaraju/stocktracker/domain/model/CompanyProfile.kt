package com.nagaraju.stocktracker.domain.model

/**
 * Company metadata displayed on the stock details screen header.
 */
data class CompanyProfile(
    val symbol: String,
    val name: String,
    val exchange: String,
    val industry: String,
    val logoUrl: String,
    val marketCap: Double,
    val currency: String,
    val country: String,
)
