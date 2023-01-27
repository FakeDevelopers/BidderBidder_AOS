package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.LoginInfo
import com.fakedevelopers.domain.repository.LoginWithSocialRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val repository: LoginWithSocialRepository
) {
    suspend operator fun invoke(idToken: String): Result<LoginInfo> =
        repository.loginWithGoogle(idToken)
}
