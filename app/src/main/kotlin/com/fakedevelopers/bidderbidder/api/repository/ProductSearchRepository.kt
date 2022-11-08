package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.ProductSearchService
import retrofit2.Response
import javax.inject.Inject

class ProductSearchRepository @Inject constructor(
    private val service: ProductSearchService
) {
    suspend fun getProductSearchRank(listCount: Int): Response<List<String>> {
        return service.getProductSearchRank(listCount)
    }
}
