package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.ChatService
import retrofit2.Response
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val service: ChatService
) {
    suspend fun getStreamUserToken(
        id: Long
    ): Response<String> {
        return service.getStreamUserToken(id)
    }
}
