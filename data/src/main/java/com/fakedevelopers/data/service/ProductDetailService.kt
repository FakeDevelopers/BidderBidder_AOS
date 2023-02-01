package com.fakedevelopers.data.service

import com.fakedevelopers.domain.model.ProductDetailInfo
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductDetailService {

    @GET("product/getProductInfo/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: Long
    ): ProductDetailInfo

    @FormUrlEncoded
    @POST("product/{productId}/bid")
    suspend fun postProductBid(
        @Path("productId") productId: Long,
        @Field("userId") userId: Long,
        @Field("bid") bid: Long
    ): String
}
