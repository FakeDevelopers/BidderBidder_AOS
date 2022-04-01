package com.fakedevelopers.ddangddangmarket.api.service

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {
    @FormUrlEncoded
    @POST("user/login")
    suspend fun loginRequest(@Field("email") email: String, @Field("passwd") passwd: String): Response<String>
}
