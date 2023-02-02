package com.fakedevelopers.domain.repository

interface ChatRepository {
    suspend fun getStreamUserToken(
        id: Long
    ): Result<String>
}
