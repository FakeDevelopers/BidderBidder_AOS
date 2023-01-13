package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.ProductDetailService
import com.fakedevelopers.presentation.ui.productDetail.ProductDetailDto
import retrofit2.Response
import javax.inject.Inject

class ProductDetailRepository @Inject constructor(
    private val service: ProductDetailService
) {
    suspend fun getProductDetail(
        productId: Long
    ): Response<ProductDetailDto> {
        return service.getProductDetail(productId)
    }

    suspend fun postProductBid(
        productId: Long,
        userId: Long,
        bid: Long
    ): Response<String> {
        return service.postProductBid(productId, userId, bid)
    }
}
