package com.fakedevelopers.bidderbidder.api.service

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserLoginService {
    @FormUrlEncoded
    @POST("user/login")
    suspend fun postLogin(@Field("email") email: String,@Field("passwd") passwd: String): Response<String>
}
