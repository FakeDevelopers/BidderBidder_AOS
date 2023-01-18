package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.LoginWithEmailRepository
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val repository: LoginWithEmailRepository
) {
    suspend operator fun invoke(email: String, passwd: String) =
        repository.loginWithEmail(email, passwd)
}
