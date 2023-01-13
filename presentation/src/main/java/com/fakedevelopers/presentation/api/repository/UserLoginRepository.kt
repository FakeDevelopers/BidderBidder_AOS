package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.UserLoginService
import retrofit2.Response
import javax.inject.Inject

class UserLoginRepository @Inject constructor(
    private val service: UserLoginService
) {
    suspend fun postLogin(email: String, passwd: String): Response<String> {
        return service.postLogin(email, passwd)
    }
}
