package com.fakedevelopers.bidderbidder.api.service

import retrofit2.Response
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserLoginService {
    @FormUrlEncoded
    @POST("user/login")
    suspend fun postLogin(email: String, passwd: String): Response<String>
}
