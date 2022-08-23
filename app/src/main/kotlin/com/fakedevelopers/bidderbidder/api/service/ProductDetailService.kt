package com.fakedevelopers.bidderbidder.api.service

import com.fakedevelopers.bidderbidder.ui.product_detail.ProductDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductDetailService {
    @GET("product/getProductInfo/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: Long
    ): Response<ProductDetailDto>
}
