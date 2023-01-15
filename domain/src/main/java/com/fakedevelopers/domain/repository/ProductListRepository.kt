package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.ProductItem

interface ProductListRepository {
    suspend fun getProductList(
        searchWord: String,
        searchType: Int,
        isInitialize: Boolean,
        count: Int
    ): Result<List<ProductItem>>
}
