package com.nagaraju.stocktracker.domain.usecase.stock

import com.nagaraju.stocktracker.domain.result.Result
import com.nagaraju.stocktracker.domain.model.CompanyProfile
import com.nagaraju.stocktracker.domain.repository.StockRepository
import javax.inject.Inject

class GetCompanyProfileUseCase @Inject constructor(
    private val stockRepository: StockRepository,
) {
    suspend operator fun invoke(symbol: String): Result<CompanyProfile> =
        stockRepository.getCompanyProfile(symbol)
}
