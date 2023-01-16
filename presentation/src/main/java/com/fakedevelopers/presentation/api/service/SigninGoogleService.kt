package com.fakedevelopers.presentation.api.service

import com.fakedevelopers.presentation.ui.loginType.SigninGoogleDto
import retrofit2.Response
import retrofit2.http.POST

interface SigninGoogleService {
    @POST("user/signin-google")
    suspend fun postSigninGoogle(): Response<SigninGoogleDto>
}
