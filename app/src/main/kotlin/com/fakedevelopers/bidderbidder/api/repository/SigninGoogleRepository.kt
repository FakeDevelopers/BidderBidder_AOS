package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.SigninGoogleService
import com.fakedevelopers.bidderbidder.ui.loginType.SigninGoogleDto
import retrofit2.Response
import javax.inject.Inject

class SigninGoogleRepository @Inject constructor(
    private val service: SigninGoogleService
) {
    suspend fun postSigninGoogle(): Response<SigninGoogleDto> {
        return service.postSigninGoogle()
    }
}
