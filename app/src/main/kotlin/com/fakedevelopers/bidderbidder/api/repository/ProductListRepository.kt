package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.ProductListService
import com.fakedevelopers.bidderbidder.ui.productList.ProductListDto
import retrofit2.Response
import javax.inject.Inject

class ProductListRepository @Inject constructor(
    private val service: ProductListService
) {
    suspend fun getProductList(
        searchWord: String,
        searchType: Int,
        listCount: Int,
        startNumber: Long
    ): Response<List<ProductListDto>> {
        return service.getProductList(searchWord, searchType, listCount, startNumber)
    }
}
