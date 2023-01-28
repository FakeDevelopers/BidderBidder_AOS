package com.fakedevelopers.presentation.api.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface ProductModificationService {
    @Multipart
    @POST("product/getProductInfo/{productId}/modify")
    suspend fun postProductModification(
        @Path("productId") productId: Long,
        @Part files: List<MultipartBody.Part>,
        @PartMap params: HashMap<String, RequestBody>
    ): Response<String>
}
