package com.minseonglove.domain.usecase

import com.minseonglove.domain.model.ProductItem
import com.minseonglove.domain.repository.ProductListRepository
import javax.inject.Inject

class GetProductListUseCase @Inject constructor(
    private val repository: ProductListRepository
) {
    suspend operator fun invoke(
        searchWord: String,
        searchType: Int,
        isInitialize: Boolean,
        count: Int
    ): Result<List<ProductItem>> =
        repository.getProductList(searchWord, searchType, isInitialize, count)
}
