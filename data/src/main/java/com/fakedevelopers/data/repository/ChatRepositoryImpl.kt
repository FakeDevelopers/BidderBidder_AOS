package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.ChatService
import com.fakedevelopers.domain.repository.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val service: ChatService
) : ChatRepository {
    override suspend fun getStreamUserToken(
        id: Long
    ): Result<String> =
        runCatching {
            service.getStreamUserToken(id)
        }
}
