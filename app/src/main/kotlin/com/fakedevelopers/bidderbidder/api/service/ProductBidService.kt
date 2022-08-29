package com.fakedevelopers.bidderbidder.api.service

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductBidService {
    @FormUrlEncoded
    @POST("product/{productId}/bid")
    suspend fun postProductBid(
        @Path("productId") productId: Long,
        @Field("userId") userId: Long,
        @Field("bid") bid: Long
    ): Response<String>
}
