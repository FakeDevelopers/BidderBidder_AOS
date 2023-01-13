package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.SigninGoogleService
import com.fakedevelopers.presentation.ui.loginType.SigninGoogleDto
import retrofit2.Response
import javax.inject.Inject

class SigninGoogleRepository @Inject constructor(
    private val service: SigninGoogleService
) {
    suspend fun postSigninGoogle(): Response<SigninGoogleDto> {
        return service.postSigninGoogle()
    }
}
