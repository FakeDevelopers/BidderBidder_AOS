package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.ProductRegistrationService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class ProductRegistrationRepository @Inject constructor(
    private val service: ProductRegistrationService
) {
    suspend fun postProductRegistration(
        files: List<MultipartBody.Part>,
        params: HashMap<String, RequestBody>
    ): Response<String> {
        return service.postProductRegistration(files, params)
    }
}
