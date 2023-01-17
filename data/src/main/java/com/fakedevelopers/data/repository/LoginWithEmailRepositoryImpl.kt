package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.LoginWithEmailService
import com.fakedevelopers.domain.repository.LoginWithEmailRepository
import javax.inject.Inject

class LoginWithEmailRepositoryImpl @Inject constructor(
    private val service: LoginWithEmailService
) : LoginWithEmailRepository {
    override suspend fun loginWithEmail(email: String, passwd: String): Result<String> {
        runCatching {
            service.loginWithEmail(email, passwd)
        }.onSuccess {
            return Result.success(it)
        }.onFailure {
            return Result.failure(it)
        }
        return Result.failure(Exception())
    }
}
