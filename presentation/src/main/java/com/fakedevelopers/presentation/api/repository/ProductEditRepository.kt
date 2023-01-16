package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.ProductEditService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class ProductEditRepository @Inject constructor(
    private val service: ProductEditService
) {
    suspend fun postProductEdit(
        productId: Long,
        files: List<MultipartBody.Part>,
        params: HashMap<String, RequestBody>
    ): Response<String> {
        return service.postProductEdit(productId, files, params)
    }
}
