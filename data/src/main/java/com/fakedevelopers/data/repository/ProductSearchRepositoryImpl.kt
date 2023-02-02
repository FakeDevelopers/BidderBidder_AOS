package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.ProductSearchService
import com.fakedevelopers.domain.repository.ProductSearchRepository
import javax.inject.Inject

class ProductSearchRepositoryImpl @Inject constructor(
    private val service: ProductSearchService
) : ProductSearchRepository {
    override suspend fun getProductSearchRank(listCount: Int): Result<List<String>> =
        runCatching {
            service.getProductSearchRank(listCount)
        }
}
