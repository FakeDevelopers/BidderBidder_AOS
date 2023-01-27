package com.fakedevelopers.data.service

import com.fakedevelopers.domain.model.LoginInfo
import retrofit2.http.POST

interface LoginWithSocialService {
    @POST("user/signin-google")
    suspend fun loginWithGoogle(): LoginInfo
}
