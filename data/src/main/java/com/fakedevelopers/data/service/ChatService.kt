package com.fakedevelopers.data.service

import retrofit2.http.GET
import retrofit2.http.Path

interface ChatService {
    @GET("chat/token/{id}")
    suspend fun getStreamUserToken(@Path("id") id: Long): String
}
