package com.minseonglove.data.repository

import com.minseonglove.data.service.ProductListService
import com.minseonglove.domain.model.LastProductException
import com.minseonglove.domain.model.NotCompletedException
import com.minseonglove.domain.model.ProductItem
import com.minseonglove.domain.repository.ProductListRepository
import javax.inject.Inject

class ProductListRepositoryImpl @Inject constructor(
    private val service: ProductListService
) : ProductListRepository {

    private var startNumber = LATEST_PRODUCT_ID
    private var isLoading = false
    private var isLastProduct = false

    override suspend fun getProductList(
        searchWord: String,
        searchType: Int,
        isInitialize: Boolean,
        count: Int
    ): Result<List<ProductItem>> {
        if (isLoading) {
            return Result.failure(NotCompletedException())
        }
        if (isInitialize) {
            startNumber = LATEST_PRODUCT_ID
            isLastProduct = false
        }
        if (isLastProduct) {
            return Result.failure(LastProductException())
        }
        isLoading = true
        runCatching {
            service.getProductList(searchWord, searchType, count, startNumber)
        }.onSuccess { productItems ->
            startNumber = productItems.lastOrNull()?.productId ?: LATEST_PRODUCT_ID
            if (productItems.size < count) {
                isLastProduct = true
            }
            isLoading = false
            return Result.success(productItems)
        }.onFailure {
            isLoading = false
            return Result.failure(it)
        }
        return Result.failure(Exception())
    }

    companion object {
        private const val LATEST_PRODUCT_ID = -1L
    }
}
