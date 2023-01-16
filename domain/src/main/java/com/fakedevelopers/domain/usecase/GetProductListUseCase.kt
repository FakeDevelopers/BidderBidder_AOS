package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.ProductItem
import com.fakedevelopers.domain.repository.ProductListRepository
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
