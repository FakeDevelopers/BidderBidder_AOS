package com.minseonglove.domain.repository

import com.minseonglove.domain.model.ProductListType

interface ProductListRepository {
    suspend fun getProductList(
        searchWord: String,
        searchType: Int,
        isInitialize: Boolean
    ): List<ProductListType>

    fun isLoadingAvailable(): Boolean
}
