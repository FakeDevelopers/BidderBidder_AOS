package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ProductSearchRepository
import javax.inject.Inject

class GetProductSearchRankUseCase @Inject constructor(
    private val repository: ProductSearchRepository
) {
    suspend operator fun invoke(listCount: Int): Result<List<String>> =
        repository.getProductSearchRank(listCount)
}
