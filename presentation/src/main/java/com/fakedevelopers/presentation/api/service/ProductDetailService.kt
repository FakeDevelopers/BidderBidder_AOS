package com.fakedevelopers.presentation.api.service

import com.fakedevelopers.presentation.ui.productDetail.ProductDetailDto
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductDetailService {
    @GET("product/getProductInfo/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: Long
    ): Response<ProductDetailDto>

    @FormUrlEncoded
    @POST("product/{productId}/bid")
    suspend fun postProductBid(
        @Path("productId") productId: Long,
        @Field("userId") userId: Long,
        @Field("bid") bid: Long
    ): Response<String>
}