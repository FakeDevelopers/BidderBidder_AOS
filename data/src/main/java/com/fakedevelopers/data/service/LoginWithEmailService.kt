package com.fakedevelopers.data.service

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginWithEmailService {
    @FormUrlEncoded
    @POST("user/login")
    suspend fun loginWithEmail(
        @Field("email") email: String,
        @Field("password") passwd: String
    ): String
}
