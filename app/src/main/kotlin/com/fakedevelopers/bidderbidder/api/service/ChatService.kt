package com.fakedevelopers.bidderbidder.api.service

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatService {
    @GET("chat/token/{id}")
    suspend fun getStreamUserToken(@Path("id") id: Int): Response<String>
}
