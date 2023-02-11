package com.fakedevelopers.data.service

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface ProductEditorService {
    @Multipart
    @POST("product/modifyProductInfo/{productId}")
    suspend fun postProductModification(
        @Path("productId") productId: Long,
        @PartMap productEditorInfo: Map<String, String>,
        @Part files: List<MultipartBody.Part>
    ): String

    @Multipart
    @POST("product/write")
    suspend fun postProductRegistration(
        @PartMap productEditorInfo: Map<String, String>,
        @Part files: List<MultipartBody.Part>
    ): String

    @Multipart
    @POST("product/deleteProduct/{productId}")
    suspend fun postDeleteProduct(
        @Path("productId") productId: Long
    ): Boolean

    @Multipart
    @POST("product/checkUserIsSame/{productId}")
    suspend fun postCheckUserIsSame(
        @Path("productId") productId: Long
    ): Boolean
}
