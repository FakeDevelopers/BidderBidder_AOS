package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.LoginInfo

interface LoginWithSocialRepository {
    suspend fun loginWithGoogle(idToken: String): Result<LoginInfo>
}
