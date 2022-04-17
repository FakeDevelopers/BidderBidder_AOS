package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.UserProductRegistrationService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class UserProductRegistrationRepository @Inject constructor(
    private val service: UserProductRegistrationService
) {
    suspend fun postProductRegistration(files: List<MultipartBody.Part>, params: HashMap<String, RequestBody>): Response<String> {
        return service.postProductRegistration(files, params)
    }
}
