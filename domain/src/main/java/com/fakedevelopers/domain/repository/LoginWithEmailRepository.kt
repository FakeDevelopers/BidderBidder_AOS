package com.fakedevelopers.domain.repository

interface LoginWithEmailRepository {
    suspend fun loginWithEmail(email: String, passwd: String): Result<String>
}
