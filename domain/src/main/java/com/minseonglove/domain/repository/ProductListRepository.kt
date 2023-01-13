package com.minseonglove.domain.repository

import com.minseonglove.domain.model.ProductItem

interface ProductListRepository {
    suspend fun getProductList(
        searchWord: String,
        searchType: Int,
        isInitialize: Boolean,
        count: Int
    ): Result<List<ProductItem>>
}
