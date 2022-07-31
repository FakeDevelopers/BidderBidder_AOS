package com.fakedevelopers.bidderbidder.api.service

import com.fakedevelopers.bidderbidder.ui.login_type.SigninGoogleDto
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface SigninGoogleService {
    @POST("user/signin-google")
    suspend fun postSigninGoogle(@Header("Authorization") authorization: String): Response<SigninGoogleDto>
}
