package com.fakedevelopers.bidderbidder.api.service

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface UserProductRegistrationService {
    @Multipart
    @POST("board/write")
    suspend fun postProductRegistration(@Part files: List<MultipartBody.Part>, @PartMap params: HashMap<String, RequestBody>): Response<String>
}
