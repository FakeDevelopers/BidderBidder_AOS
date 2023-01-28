package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.ProductModificationService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class ProductModificationRepository @Inject constructor(
    private val service: ProductModificationService
) {
    suspend fun postProductModification(
        productId: Long,
        files: List<MultipartBody.Part>,
        params: HashMap<String, RequestBody>
    ): Response<String> {
        return service.postProductModification(productId, files, params)
    }
}
