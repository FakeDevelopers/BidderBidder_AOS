package com.fakedevelopers.bidderbidder.api.service

import com.fakedevelopers.bidderbidder.ui.product_detail.ProductDetailDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductDetailService {
    @GET("product/getProductInfo/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: Long
    ): Response<ProductDetailDto>

    @POST("product/{productId}/bid")
    suspend fun postProductBid(
        @Path("productId") productId: Long,
        @Query("userId") userId: Long,
        @Query("bid") bid: Long
    ): Response<String>
}
