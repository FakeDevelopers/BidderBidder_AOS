package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.ProductDetailService
import com.fakedevelopers.bidderbidder.ui.product_detail.ProductDetailDto
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
}
