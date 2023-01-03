package com.minseonglove.data.repository

import com.minseonglove.data.service.ProductListService
import com.minseonglove.domain.model.ProductItem
import com.minseonglove.domain.model.ProductListType
import com.minseonglove.domain.model.ProductReadMore
import com.minseonglove.domain.repository.ProductListRepository
import javax.inject.Inject

class ProductListRepositoryImpl @Inject constructor(
    private val service: ProductListService
) : ProductListRepository {

    private val currentProductItems = mutableListOf<ProductListType>()
    private var startNumber = LATEST_PRODUCT_ID
    private var isLoading = false
    private var isReadMoreVisible = true
    private var isLastProduct = false

    override suspend fun getProductList(
        searchWord: String,
        searchType: Int,
        isInitialize: Boolean
    ): List<ProductListType> {
        if (isInitialize) {
            isLastProduct = false
            currentProductItems.clear()
        } else {
            currentProductItems.remove(ProductReadMore)
        }
        isLoading = true
        val result = service.getProductList(searchWord, searchType, LIST_COUNT, startNumber)
        handleResult(result)
        isLoading = false
        return currentProductItems
    }

    override fun isLoadingAvailable(): Boolean =
        isLoading || isReadMoreVisible || isLastProduct

    private fun handleResult(result: List<ProductItem>) {
        startNumber = result.lastOrNull()?.productId ?: LATEST_PRODUCT_ID
        if (result.size < LIST_COUNT) {
            isReadMoreVisible = false
            isLastProduct = true
        }
        currentProductItems.addAll(result)
        if (isReadMoreVisible) {
            currentProductItems.add(ProductReadMore)
        }
    }

    companion object {
        private const val LATEST_PRODUCT_ID = -1L
        private const val LIST_COUNT = 20
    }
}
